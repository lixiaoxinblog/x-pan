package com.xiaoxin.pan.server.modules.recycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.server.common.envent.file.FilePhysicalDeleteEvent;
import com.xiaoxin.pan.server.common.envent.file.FileRestoreEvent;
import com.xiaoxin.pan.server.modules.file.context.QueryFileListContext;
import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.xiaoxin.pan.server.modules.file.enums.DelFlagEnum;
import com.xiaoxin.pan.server.modules.file.service.XPanUserFileService;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import com.xiaoxin.pan.server.modules.recycle.context.DeleteContext;
import com.xiaoxin.pan.server.modules.recycle.context.QueryRecycleFileListContext;
import com.xiaoxin.pan.server.modules.recycle.context.RestoreContext;
import com.xiaoxin.pan.server.modules.recycle.service.XPanUserRecycleService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Service
public class XPanUserRecycleServiceImpl implements XPanUserRecycleService, ApplicationContextAware {
    @Autowired
    private XPanUserFileService xPanUserFileService;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 回收站文件列表
     * 调用 用户文件业务功能
     *
     * @param queryRecycleFileListContext
     * @return
     */
    @Override
    public List<XPanUserFileVO> list(QueryRecycleFileListContext queryRecycleFileListContext) {
        QueryFileListContext queryFileListContext = new QueryFileListContext();
        queryFileListContext.setUserId(queryRecycleFileListContext.getUserId());
        queryFileListContext.setDelFlag(DelFlagEnum.YES.getCode());
        return xPanUserFileService.getFileList(queryFileListContext);
    }

    /**
     * 批量欢迎回收站文件
     * 1.检查用户时候有文件操作权限
     * 2.检查要还原的文件名称是不是被占用
     * 3.执行文件还原操作
     * 4.执行文件还原的后置操作
     *
     * @param restoreContext
     */
    @Override
    public void restore(RestoreContext restoreContext) {
        checkRestorePermission(restoreContext);
        checkRestoreFilename(restoreContext);
        doRestore(restoreContext);
        afterRestore(restoreContext);
    }

    /**
     * 文件彻底删除
     * 1、校验操作权限
     * 2、递归查找所有子文件
     * 3、执行文件删除的动作
     * 4、删除后的后置动作
     *
     * @param deleteContext
     */
    @Override
    public void delete(DeleteContext deleteContext) {
        checkFileDeletePermission(deleteContext);
        findAllFileRecords(deleteContext);
        doDelete(deleteContext);
        afterDelete(deleteContext);
    }


    /**
     * 文件彻底删除之后的后置函数
     *发送一个文件彻底删除的事件
     *
     * @param deleteContext
     */
    private void afterDelete(DeleteContext deleteContext) {
        FilePhysicalDeleteEvent filePhysicalDeleteEvent = new FilePhysicalDeleteEvent(this, deleteContext.getAllRecords());
        applicationContext.publishEvent(filePhysicalDeleteEvent);
    }

    /**
     * 执行文件删除的动作
     *
     * @param deleteContext
     */
    private void doDelete(DeleteContext deleteContext) {
        List<Long> fileIdList = deleteContext.getFileIdList();
        if (!xPanUserFileService.removeByIds(fileIdList)) {
            throw new XPanBusinessException("文件删除失败！");
        }
    }

    /**
     * 递归查询所有的子文件
     *
     * @param deleteContext
     */
    private void findAllFileRecords(DeleteContext deleteContext) {
        List<XPanUserFile> records = deleteContext.getRecords();
        List<XPanUserFile> allRecords = xPanUserFileService.findAllFileRecords(records);
        deleteContext.setAllRecords(allRecords);
    }

    /**
     * 校验文件删除的操作权限
     */
    private void checkFileDeletePermission(DeleteContext deleteContext) {
        LambdaQueryWrapper<XPanUserFile> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(XPanUserFile::getUserId, deleteContext.getUserId());
        queryWrapper.in(XPanUserFile::getFileId, deleteContext.getFileIdList());
        List<XPanUserFile> records = xPanUserFileService.list(queryWrapper);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(records) || records.size() != deleteContext.getFileIdList().size()) {
            throw new XPanBusinessException("您无权删除该文件");
        }
        deleteContext.setRecords(records);
    }

    /**
     * 还原文件后置操作
     *
     * @param restoreContext
     */
    private void afterRestore(RestoreContext restoreContext) {
        FileRestoreEvent fileRestoreEvent = new FileRestoreEvent(this, restoreContext.getFileIdList());
        applicationContext.publishEvent(fileRestoreEvent);
    }

    /**
     * 执行文件还原操作
     *
     * @param restoreContext
     */
    private void doRestore(RestoreContext restoreContext) {
        List<XPanUserFile> records = restoreContext.getRecords();
        records.forEach(record -> {
            record.setDelFlag(DelFlagEnum.NO.getCode());
            record.setUpdateUser(restoreContext.getUserId());
            record.setUpdateTime(new Date());
        });
        boolean updateFlag = xPanUserFileService.updateBatchById(records);
        if (!updateFlag) {
            throw new XPanBusinessException("文件还原失败！");
        }
    }


    /**
     * 检查要还原的文件名称是不是被占用
     * 1、要还原的文件列表中有同一个文件夹下面相同名称的文件 不允许还原
     * 2、要还原的文件当前的父文件夹下面存在同名文件，我们不允许还原
     *
     * @param restoreContext
     */
    private void checkRestoreFilename(RestoreContext restoreContext) {
        List<XPanUserFile> records = restoreContext.getRecords();
        Set<String> filenameSet = records.stream()
                .map(record -> record.getFilename() + XPanConstants.COMMON_SEPARATOR + record.getParentId())
                .collect(Collectors.toSet());
        if (filenameSet.size() != records.size()) {
            throw new XPanBusinessException("文件还原失败，该还原文件中存在同名文件，请逐个还原并重命名");
        }
        //查询当前用户所有文件
        LambdaQueryWrapper<XPanUserFile> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(XPanUserFile::getUserId, restoreContext.getUserId());
        queryWrapper.eq(XPanUserFile::getDelFlag, DelFlagEnum.NO.getCode());
        List<XPanUserFile> allXPanUerFile = xPanUserFileService.list(queryWrapper);
        for (XPanUserFile record : records) {
            String filename = record.getFilename();
            Long parentId = record.getParentId();
            List<XPanUserFile> result = allXPanUerFile.stream()
                    .filter(xPanUserFile -> xPanUserFile.getFilename().equals(filename) &&
                            xPanUserFile.getParentId().equals(parentId))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(result)) {
                throw new XPanBusinessException("文件: " + filename + " 还原失败，该文件夹下面已经存在了相同名称的文件或者文件夹，请重命名之后再执行文件还原操作");
            }
        }
    }

    /**
     * 检查文件还原权限
     *
     * @param restoreContext
     */
    private void checkRestorePermission(RestoreContext restoreContext) {
        List<Long> fileIdList = restoreContext.getFileIdList();
        List<XPanUserFile> xPanUserFiles = xPanUserFileService.listByIds(fileIdList);
        if (CollectionUtils.isEmpty(xPanUserFiles)) {
            throw new XPanBusinessException("文件还原失败");
        }
        Set<Long> userIdSet = xPanUserFiles.stream()
                .map(XPanUserFile::getUserId).collect(Collectors.toSet());
        if (userIdSet.size() > 1) {
            throw new XPanBusinessException("您无权执行文件还原");
        }
        if (!userIdSet.contains(restoreContext.getUserId())) {
            throw new XPanBusinessException("您无权执行文件还原");
        }
        restoreContext.setRecords(xPanUserFiles);
    }


}
