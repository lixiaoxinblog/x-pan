package com.xiaoxin.pan.server.common.listenner.file;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.server.common.envent.file.FilePhysicalDeleteEvent;
import com.xiaoxin.pan.server.common.envent.log.ErrorLogEvent;
import com.xiaoxin.pan.server.modules.file.entity.XPanFile;
import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.xiaoxin.pan.server.modules.file.enums.FolderFlagEnum;
import com.xiaoxin.pan.server.modules.file.service.XPanFileService;
import com.xiaoxin.pan.server.modules.file.service.XPanUserFileService;
import com.xiaoxin.pan.storge.engine.core.StorageEngine;
import com.xiaoxin.pan.storge.engine.core.context.DeleteFileContext;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件物理删除监听器
 */
@Component
public class FilePhysicalDeleteEventListener implements ApplicationContextAware {

    @Autowired
    private XPanFileService xPanFileService;

    @Autowired
    private XPanUserFileService xPanUserFileService;

    @Autowired
    private StorageEngine storageEngine;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    /**
     * 监听文件物理删除事件执行器
     * 该执行器是一个资源释放器，释放被物理删除的文件列表中关联的实体文件记录
     * 1、查询所有无引用的实体文件记录
     * 2、删除记录
     * 3、物理清理文件（委托文件存储引擎）
     *
     * @param event
     */
    @EventListener(classes = FilePhysicalDeleteEvent.class)
    @Async(value = "eventListenerTaskExecutor")
    public void physicalDeleteFile(FilePhysicalDeleteEvent event) {
        List<XPanUserFile> allRecords = event.getAllRecords();
        if (CollectionUtils.isEmpty(allRecords)) {
            return;
        }
        List<Long> realFileIdList = findAllUnusedRealFileIdList(allRecords);
        List<XPanFile> xPanFiles = xPanFileService.listByIds(realFileIdList);
        if (CollectionUtils.isEmpty(xPanFiles)) {
            return;
        }
        if (!xPanFileService.removeByIds(realFileIdList)) {
            applicationContext.publishEvent(new ErrorLogEvent(this, "实体文件记录：" + JSON.toJSONString(realFileIdList), XPanConstants.ZERO_LONG));
            return;
        }
        physicalDeleteFileByStorageEngine(xPanFiles);
    }

    /**
     * 委托文件存储引擎执行物理文件的删除
     *
     * @param xPanFiles
     */
    private void physicalDeleteFileByStorageEngine(List<XPanFile> xPanFiles) {
        List<String> realPathList = xPanFiles.stream()
                .map(XPanFile::getRealPath)
                .collect(Collectors.toList());
        DeleteFileContext deleteFileContext = new DeleteFileContext();
        deleteFileContext.setRealFilePathList(realPathList);
        try {
            storageEngine.delete(deleteFileContext);
        } catch (IOException e) {
            applicationContext.publishEvent(new ErrorLogEvent(this, "实体文件：" + JSON.toJSONString(realPathList) + "， 物理删除失败，请执行手动删除", XPanConstants.ZERO_LONG));
        }
    }

    /**
     * 查找所有没有被引用的真实文件记录ID集合
     *
     * @param allRecords
     * @return
     */
    private List<Long> findAllUnusedRealFileIdList(List<XPanUserFile> allRecords) {
        return allRecords.stream()
                .filter(record -> Objects.equals(record.getFolderFlag(), FolderFlagEnum.NO.getCode()))
                .filter(this::isUnused)
                .map(XPanUserFile::getRealFileId)
                .collect(Collectors.toList());
    }

    /**
     * 校验文件的真实文件ID是不是没有被引用了
     *
     * @param record
     * @return
     */
    private boolean isUnused(XPanUserFile record) {
        QueryWrapper<XPanUserFile> queryWrapper = Wrappers.query();
        queryWrapper.eq("real_file_id", record.getRealFileId());
        return xPanUserFileService.count(queryWrapper) == XPanConstants.ZERO_INT;
    }
}
