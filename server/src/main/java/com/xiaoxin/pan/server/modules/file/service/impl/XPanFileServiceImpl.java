package com.xiaoxin.pan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.core.utils.FileUtils;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.common.envent.log.ErrorLogEvent;
import com.xiaoxin.pan.server.modules.file.context.FileSaveContext;
import com.xiaoxin.pan.server.modules.file.context.QueryFileListContext;
import com.xiaoxin.pan.server.modules.file.context.QueryRealFileListContext;
import com.xiaoxin.pan.server.modules.file.entity.XPanFile;
import com.xiaoxin.pan.server.modules.file.service.XPanFileService;
import com.xiaoxin.pan.server.modules.file.mapper.XPanFileMapper;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import com.xiaoxin.pan.storge.engine.core.StorageEngine;
import com.xiaoxin.pan.storge.engine.core.context.DeleteFileContext;
import com.xiaoxin.pan.storge.engine.core.context.StoreFileContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author xiaoxin
 * @description 针对表【x_pan_file(物理文件信息表)】的数据库操作Service实现
 * @createDate 2024-07-26 12:48:16
 */
@Service
public class XPanFileServiceImpl extends ServiceImpl<XPanFileMapper, XPanFile>
        implements XPanFileService, ApplicationContextAware {

    @Autowired
    private StorageEngine storageEngine;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 根据条件查询用户的实际文件列表
     *
     * @param context
     * @return
     */
    @Override
    public List<XPanFile> getFileList(QueryRealFileListContext context) {
        Long userId = context.getUserId();
        String identifier = context.getIdentifier();
        LambdaQueryWrapper<XPanFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(userId), XPanFile::getCreateUser, userId);
        queryWrapper.eq(StringUtils.isNotBlank(identifier), XPanFile::getIdentifier, identifier);
        return list(queryWrapper);
    }

    /**
     * 上传单文件并保存实体记录
     * <p>
     * 1、上传单文件
     * 2、保存实体记录
     *
     * @param fileSaveContext
     */
    @Override
    public void saveFile(FileSaveContext fileSaveContext) {
        storeMultipartFile(fileSaveContext);
        XPanFile record = doSaveFile(fileSaveContext.getFilename(),
                fileSaveContext.getRealPath(),
                fileSaveContext.getTotalSize(),
                fileSaveContext.getIdentifier(),
                fileSaveContext.getUserId());
        fileSaveContext.setRecord(record);
    }

    /**
     * 保存文件实体记录
     *
     * @param filename
     * @param realPath
     * @param totalSize
     * @param identifier
     * @param userId
     * @return
     */
    private XPanFile doSaveFile(String filename, String realPath, Long totalSize, String identifier, Long userId) {
        XPanFile record = assembleXPanFile(filename, realPath, totalSize, identifier, userId);
        if (!save(record)) {
            try {
                DeleteFileContext deleteFileContext = new DeleteFileContext();
                deleteFileContext.setRealFilePathList(Lists.newArrayList(realPath));
                storageEngine.delete(deleteFileContext);
            } catch (IOException e) {
                e.printStackTrace();
                ErrorLogEvent errorLogEvent = new ErrorLogEvent(this,"文件物理删除失败，请执行手动删除！文件路径:"+realPath,userId);
                applicationContext.publishEvent(errorLogEvent);
            }
        }
        return record;
    }

    /**
     * 拼装文件实体对象
     *
     * @param filename
     * @param realPath
     * @param totalSize
     * @param identifier
     * @param userId
     * @return
     */
    private XPanFile assembleXPanFile(String filename, String realPath, Long totalSize, String identifier, Long userId) {
        XPanFile record = new XPanFile();
        record.setFileId(IdUtil.get());
        record.setFilename(filename);
        record.setRealPath(realPath);
        record.setFileSize(String.valueOf(totalSize));
        record.setFileSizeDesc(FileUtils.byteCountToDisplaySize(totalSize));
        record.setFileSuffix(FileUtils.getFileSuffix(filename));
        record.setIdentifier(identifier);
        record.setCreateUser(userId);
        record.setCreateTime(new Date());
        return record;
    }

    /**
     * 上传单文件
     * 该方法委托文件存储引擎实现
     *
     * @param fileSaveContext
     */
    private void storeMultipartFile(FileSaveContext fileSaveContext) {
        try {
            StoreFileContext storeFileContext = new StoreFileContext();
            storeFileContext.setFilename(fileSaveContext.getFilename());
            storeFileContext.setInputStream(fileSaveContext.getFile().getInputStream());
            storeFileContext.setTotalSize(fileSaveContext.getTotalSize());
            storageEngine.store(storeFileContext);
            fileSaveContext.setRealPath(storeFileContext.getRealPath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new XPanBusinessException("文件上传失败！");
        }
    }


}




