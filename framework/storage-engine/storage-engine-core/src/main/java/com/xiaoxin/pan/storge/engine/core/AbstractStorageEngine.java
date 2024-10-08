package com.xiaoxin.pan.storge.engine.core;

import cn.hutool.core.lang.Assert;
import com.xiaoxin.pan.cache.core.constanst.CacheConstants;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.storge.engine.core.context.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.io.IOException;
import java.util.Objects;

/**
 * 文件存储引擎模块公用抽象类
 * 具体的文件存储实现方案的公用逻辑需要抽离到该类中
 */
public abstract class AbstractStorageEngine implements StorageEngine {

    @Autowired
    private CacheManager cacheManager;

    protected Cache getCache() {
        if (Objects.isNull(cacheManager)) {
            throw new XPanBusinessException("具体的缓存实现需要引用到项目中");
        }
        return cacheManager.getCache(CacheConstants.X_PAN_CACHE_NAME);
    }

    /**
     * 存储物理文件
     * 1、参数校验
     * 2、执行动作
     *
     * @param deleteFileContext
     * @throws IOException
     */
    @Override
    public void delete(DeleteFileContext deleteFileContext) throws IOException {
        checkDeleteFileContext(deleteFileContext);
        doDelete(deleteFileContext);
    }


    @Override
    public void store(StoreFileContext storeFileContext) throws IOException {
        checkStoreFileContext(storeFileContext);
        doStore(storeFileContext);
    }


    /**
     * 存储物理文件的分片
     * 1、参数校验
     * 2、执行动作
     */
    @Override
    public void storeChunk(StoreFileChunkContext storeFileChunkContext) throws IOException {
        checkStoreFileChunkContext(storeFileChunkContext);
        doStoreChunk(storeFileChunkContext);
    }

    /**
     * 文件分片合并
     * 1、检查参数
     * 2、执行动作
     *
     * @param mergeFileContext
     */
    @Override
    public void mergeFile(MergeFileContext mergeFileContext) throws IOException {
        checkMergeFileContext(mergeFileContext);
        doMergeFile(mergeFileContext);
    }

    /**
     * 校验文件合并的上下文信息
     * @param mergeFileContext
     */
    private void checkMergeFileContext(MergeFileContext mergeFileContext) {
        Assert.notBlank(mergeFileContext.getFilename(), "文件名称不能为空");
        Assert.notBlank(mergeFileContext.getIdentifier(), "文件唯一标识不能为空");
        Assert.notNull(mergeFileContext.getUserId(), "当前登录用户的ID不能为空");
        Assert.notEmpty(mergeFileContext.getRealPathList(), "文件分片列表不能为空");
    }

    /**
     * 合并分片，下沉到子类去实现
     * @param mergeFileContext
     */
    protected abstract void doMergeFile(MergeFileContext mergeFileContext) throws IOException;

    /**
     * 执行文件分片上传
     * 下沉到子类去实现
     *
     * @param storeFileChunkContext
     */
    protected abstract void doStoreChunk(StoreFileChunkContext storeFileChunkContext) throws IOException;

    /**
     * 检查存储物理文件的分片上下文信息
     *
     * @param storeFileChunkContext
     */
    private void checkStoreFileChunkContext(StoreFileChunkContext storeFileChunkContext) {
        Assert.notBlank(storeFileChunkContext.getFilename(), "文件名称不能为空");
        Assert.notBlank(storeFileChunkContext.getIdentifier(), "文件唯一标识不能为空");
        Assert.notNull(storeFileChunkContext.getTotalSize(), "文件大小不能为空");
        Assert.notNull(storeFileChunkContext.getInputStream(), "文件分片不能为空");
        Assert.notNull(storeFileChunkContext.getTotalChunks(), "文件分片总数不能为空");
        Assert.notNull(storeFileChunkContext.getChunkNumber(), "文件分片下标不能为空");
        Assert.notNull(storeFileChunkContext.getCurrentChunkSize(), "文件分片的大小不能为空");
        Assert.notNull(storeFileChunkContext.getUserId(), "当前登录用户的ID不能为空");
    }

    /**
     * 校验存储物理文件的上下文信息
     *
     * @param storeFileContext
     */
    private void checkStoreFileContext(StoreFileContext storeFileContext) {
        Assert.notBlank(storeFileContext.getFilename(), "文件名称不能为空");
        Assert.notNull(storeFileContext.getTotalSize(), "文件的总大小不能为空");
        Assert.notNull(storeFileContext.getInputStream(), "文件不能为空");
    }

    /**
     * 执行存储物理文件的动作
     * 下沉到子类去实现
     *
     * @param storeFileContext
     * @throws IOException
     */
    protected abstract void doStore(StoreFileContext storeFileContext) throws IOException;

    /**
     * 执行删除物理文件的动作
     * 下沉到子类去实现
     *
     * @param deleteFileContext
     * @throws IOException
     */
    protected abstract void doDelete(DeleteFileContext deleteFileContext) throws IOException;

    /**
     * 校验删除物理文件的上下文信息
     *
     * @param deleteFileContext
     */
    private void checkDeleteFileContext(DeleteFileContext deleteFileContext) {
        Assert.notEmpty(deleteFileContext.getRealFilePathList(), "要删除的文件路径列表不能为空！");
    }


    /**
     * 读取文件内容写入到输出流中
     * 1、参数校验
     * 2、执行动作
     *
     * @param readFileContext
     * @throws IOException
     */
    @Override
    public void realFile(ReadFileContext readFileContext) throws IOException {
        checkReadFileContext(readFileContext);
        doRealFile(readFileContext);
    }

    /**
     * 校验读取文件上下文信息
     * @param readFileContext
     */
    private void checkReadFileContext(ReadFileContext readFileContext) {
        Assert.notBlank(readFileContext.getRealPath(), "文件真实存储路径不能为空");
        Assert.notNull(readFileContext.getOutputStream(), "文件的输出流不能为空");
    }

    /**
     * 下层到子类去实现
     * @param readFileContext
     */
    public abstract void doRealFile(ReadFileContext readFileContext) throws IOException;

    @Override
    public void rangeFile(ReadRangeFileContext readRangeFileContext) throws IOException {
        checkReadRangeFileContext(readRangeFileContext);
        doRangeFile(readRangeFileContext);
    }

    private void checkReadRangeFileContext(ReadRangeFileContext readRangeFileContext) {
        Assert.notBlank(readRangeFileContext.getRealPath(), "文件真实存储路径不能为空");
        Assert.notNull(readRangeFileContext.getOutputStream(), "文件的输出流不能为空");
        Assert.notNull(readRangeFileContext.getStart(), "文件的开始位置不能为空");
        Assert.notNull(readRangeFileContext.getEnd(), "文件的结束位置不能为空");
    }

    /**
     * 下层到子类去实现
     * @param readRangeFileContext
     */
    public abstract void doRangeFile(ReadRangeFileContext readRangeFileContext) throws IOException;
}
