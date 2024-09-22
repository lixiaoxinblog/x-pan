package com.xiaoxin.pan.server.modules.file.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.common.config.PanServerConfig;
import com.xiaoxin.pan.server.modules.file.context.FileChunkSaveContext;
import com.xiaoxin.pan.server.modules.file.converter.FileConverter;
import com.xiaoxin.pan.server.modules.file.entity.XPanFileChunk;
import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.xiaoxin.pan.server.modules.file.enums.MergeFlagEnum;
import com.xiaoxin.pan.server.modules.file.service.XPanFileChunkService;
import com.xiaoxin.pan.server.modules.file.mapper.XPanFileChunkMapper;
import com.xiaoxin.pan.storge.engine.core.StorageEngine;
import com.xiaoxin.pan.storge.engine.core.context.StoreFileChunkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

/**
 * @author xiaoxin
 * @description 针对表【x_pan_file_chunk(文件分片信息表)】的数据库操作Service实现
 * @createDate 2024-07-26 12:48:15
 */
@Service
public class XPanFileChunkServiceImpl extends ServiceImpl<XPanFileChunkMapper, XPanFileChunk>
        implements XPanFileChunkService {

    @Autowired
    private StorageEngine storageEngine;
    @Autowired
    private FileConverter fileConverter;
    @Autowired
    private PanServerConfig panServerConfig;

    /**
     * 文件分片保存
     * <p>
     * 1、保存文件分片和记录
     * 2、判断文件分片是否全部上传完成
     *
     * @param fileChunkSaveContext
     */
    @Override
    public synchronized void saveChunkFile(FileChunkSaveContext fileChunkSaveContext) {
        doSaveChunkFile(fileChunkSaveContext);
        doJudgeMergeFile(fileChunkSaveContext);
    }

    /**
     * 判断是否所有的分片均没上传完成
     */
    private void doJudgeMergeFile(FileChunkSaveContext fileChunkSaveContext) {
        QueryWrapper<XPanFileChunk> queryWrapper = Wrappers.query();
        queryWrapper.eq("identifier",fileChunkSaveContext.getIdentifier());
        queryWrapper.eq("create_user",fileChunkSaveContext.getUserId());
        int count =count(queryWrapper);
        if (count == fileChunkSaveContext.getChunkNumber()){
            fileChunkSaveContext.setMergeFlagEnum(MergeFlagEnum.READY);
        }
    }

    /**
     * 执行文件分片上传保存的操作
     * 1、委托文件存储引擎存储文件分片
     * 2、保存文件分片记录
     */
    private void doSaveChunkFile(FileChunkSaveContext fileChunkSaveContext) {
        doStoreFileChunk(fileChunkSaveContext);
        doSaveRecord(fileChunkSaveContext);
    }

    /**
     * 保存文件分片实体信息
     *
     * @param fileChunkSaveContext
     */
    private void doSaveRecord(FileChunkSaveContext fileChunkSaveContext) {
        XPanFileChunk xPanFileChunk = new XPanFileChunk();
        xPanFileChunk.setId(IdUtil.get());
        xPanFileChunk.setIdentifier(fileChunkSaveContext.getIdentifier());
        xPanFileChunk.setRealPath(fileChunkSaveContext.getRealPath());
        xPanFileChunk.setChunkNumber(fileChunkSaveContext.getChunkNumber());
        xPanFileChunk.setExpirationTime(DateUtil.offsetDay(new Date(), panServerConfig.getChunkFileExpirationDays()));
        xPanFileChunk.setCreateUser(fileChunkSaveContext.getUserId());
        xPanFileChunk.setCreateTime(new Date());
        if (!save(xPanFileChunk)) {
            throw new XPanBusinessException("文件分片上传失败！");
        }
    }

    /**
     * 委托文件存储引擎保存文件分片
     *
     * @param fileChunkSaveContext
     */
    private void doStoreFileChunk(FileChunkSaveContext fileChunkSaveContext) {
        try {
            StoreFileChunkContext storeFileChunkContext = fileConverter.fileChunkSaveContext2StoreFileChunkContext(fileChunkSaveContext);
            storeFileChunkContext.setInputStream(fileChunkSaveContext.getFile().getInputStream());
            storageEngine.storeChunk(storeFileChunkContext);
            storeFileChunkContext.setRealPath(storeFileChunkContext.getRealPath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new XPanBusinessException("文件分片上传失败！");
        }
    }

}




