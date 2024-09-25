package com.xiaoxin.pan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.core.utils.FileUtils;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.common.envent.log.ErrorLogEvent;
import com.xiaoxin.pan.server.modules.file.context.FileChunkMergeAndSaveContext;
import com.xiaoxin.pan.server.modules.file.context.FileSaveContext;
import com.xiaoxin.pan.server.modules.file.context.QueryFileListContext;
import com.xiaoxin.pan.server.modules.file.context.QueryRealFileListContext;
import com.xiaoxin.pan.server.modules.file.entity.XPanFile;
import com.xiaoxin.pan.server.modules.file.entity.XPanFileChunk;
import com.xiaoxin.pan.server.modules.file.enums.FileSuffixContextTypeEnum;
import com.xiaoxin.pan.server.modules.file.service.XPanFileChunkService;
import com.xiaoxin.pan.server.modules.file.service.XPanFileService;
import com.xiaoxin.pan.server.modules.file.mapper.XPanFileMapper;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import com.xiaoxin.pan.storge.engine.core.StorageEngine;
import com.xiaoxin.pan.storge.engine.core.context.DeleteFileContext;
import com.xiaoxin.pan.storge.engine.core.context.MergeFileContext;
import com.xiaoxin.pan.storge.engine.core.context.StoreFileContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private XPanFileChunkService xPanFileChunkService;

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
     * 合并分片并保存文件实体记录
     * 1、委托文件存储引擎合并文件分片
     * 2、保存物理文件记录
     *
     * @param fileChunkMergeAndSaveContext
     */
    @Override
    public void mergeFileChunkAndSaveFile(FileChunkMergeAndSaveContext fileChunkMergeAndSaveContext) {
        doMergeFileChunk(fileChunkMergeAndSaveContext);
        XPanFile xPanFile = doSaveFile(
                fileChunkMergeAndSaveContext.getFilename(),
                fileChunkMergeAndSaveContext.getRealPath(),
                fileChunkMergeAndSaveContext.getTotalSize(),
                fileChunkMergeAndSaveContext.getIdentifier(),
                fileChunkMergeAndSaveContext.getUserId()
        );
        fileChunkMergeAndSaveContext.setRecord(xPanFile);
    }

    /**
     * 委托文件存储引擎合并文件分片
     * 1、查询文件分片的记录
     * 2、根据文件分片的记录去合并物理文件
     * 3、删除文件分片记录
     * 4、封装合并文件的真实存储路径到上下文信息中
     *
     * @param context
     */
    private void doMergeFileChunk(FileChunkMergeAndSaveContext context) {
        QueryWrapper<XPanFileChunk> queryWrapper = Wrappers.query();
        queryWrapper.eq("identifier", context.getIdentifier());
        queryWrapper.eq("create_user", context.getUserId());
        queryWrapper.ge("expiration_time", new Date());
        List<XPanFileChunk> listFileChunk = xPanFileChunkService.list(queryWrapper);
        if (CollectionUtils.isEmpty(listFileChunk)) {
            throw new XPanBusinessException("该文件未找到分片记录");
        }
        List<String> realPathList = listFileChunk.stream()
                .sorted(Comparator.comparing(XPanFileChunk::getChunkNumber))
                .map(XPanFileChunk::getRealPath)
                .collect(Collectors.toList());
        MergeFileContext mergeFileContext = new MergeFileContext();
        mergeFileContext.setFilename(context.getFilename());
        mergeFileContext.setIdentifier(context.getIdentifier());
        mergeFileContext.setUserId(context.getUserId());
        mergeFileContext.setRealPathList(realPathList);
        try {
            storageEngine.mergeFile(mergeFileContext);
        } catch (IOException e) {
            e.printStackTrace();
            throw new XPanBusinessException("文件分片合并失败");
        }
        context.setRealPath(mergeFileContext.getRealPath());
        List<Long> fileChunkRecordIdList = listFileChunk.stream().map(XPanFileChunk::getId).collect(Collectors.toList());
        xPanFileChunkService.removeByIds(fileChunkRecordIdList);
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
                ErrorLogEvent errorLogEvent = new ErrorLogEvent(this, "文件物理删除失败，请执行手动删除！文件路径:" + realPath, userId);
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
        String contextType = FileSuffixContextTypeEnum.getContextType(FileUtils.getFileSuffix(filename));
        record.setFilePreviewContentType(contextType);
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




