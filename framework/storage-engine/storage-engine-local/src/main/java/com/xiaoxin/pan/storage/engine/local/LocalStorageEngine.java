package com.xiaoxin.pan.storage.engine.local;

import com.xiaoxin.pan.core.utils.FileUtils;
import com.xiaoxin.pan.storage.engine.local.config.LocalStorageEngineConfig;
import com.xiaoxin.pan.storge.engine.core.AbstractStorageEngine;
import com.xiaoxin.pan.storge.engine.core.context.DeleteFileContext;
import com.xiaoxin.pan.storge.engine.core.context.StoreFileChunkContext;
import com.xiaoxin.pan.storge.engine.core.context.StoreFileContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * 本地的文件存储引擎实现方案
 */
@Component
public class LocalStorageEngine extends AbstractStorageEngine {


    @Autowired
    private LocalStorageEngineConfig config;

    /**
     * 执行保存物理文件的动作
     *
     * @param storeFileContext
     */
    @Override
    protected void doStore(StoreFileContext storeFileContext) throws IOException {
        String basePath = config.getRootFilePath();
        String realFilePath = FileUtils.generateStoreFileRealPath(basePath, storeFileContext.getFilename());
        FileUtils.writeStream2File(storeFileContext.getInputStream(), new File(realFilePath), storeFileContext.getTotalSize());
        storeFileContext.setRealPath(realFilePath);
    }

    /**
     * 执行存储文件分片的动作
     *
     * @param storeFileChunkContext
     * @throws IOException
     */
    @Override
    protected void doStoreChunk(StoreFileChunkContext storeFileChunkContext) throws IOException {
        String basePath = config.getRootFileChunkPath();
        String realFilePath = FileUtils.generateStoreFileChunkRealPath(basePath, storeFileChunkContext.getIdentifier(), storeFileChunkContext.getChunkNumber());
        FileUtils.writeStream2File(storeFileChunkContext.getInputStream(), new File(realFilePath), storeFileChunkContext.getTotalSize());
        storeFileChunkContext.setRealPath(realFilePath);
    }

    /**
     * 执行删除物理文件的动作
     */
    @Override
    protected void doDelete(DeleteFileContext deleteFileContext) throws IOException {
        FileUtils.deleteFiles(deleteFileContext.getRealFilePathList());
    }

}
