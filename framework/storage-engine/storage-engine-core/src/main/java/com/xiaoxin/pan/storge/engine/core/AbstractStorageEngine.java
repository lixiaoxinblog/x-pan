package com.xiaoxin.pan.storge.engine.core;

import cn.hutool.core.lang.Assert;
import com.xiaoxin.pan.cache.core.constanst.CacheConstants;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.storge.engine.core.context.DeleteFileContext;
import com.xiaoxin.pan.storge.engine.core.context.StoreFileContext;
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
     * 校验存储物理文件的上下文信息
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
        Assert.notEmpty(deleteFileContext.getRealFilePathList(),"要删除的文件路径列表不能为空！");

    }
}
