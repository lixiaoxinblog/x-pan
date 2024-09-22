package com.xiaoxin.pan.storge.engine.core;

import com.xiaoxin.pan.cache.core.constanst.CacheConstants;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Objects;

/**
 * 文件存储引擎模块公用抽象类
 * 具体的文件存储实现方案的公用逻辑需要抽离到该类中
 */
public abstract class AbstractStorageEngine implements StorageEngine{

    @Autowired
    private CacheManager cacheManager;

    protected Cache getCache() {
        if (Objects.isNull(cacheManager)) {
            throw new XPanBusinessException("具体的缓存实现需要引用到项目中");
        }
        return cacheManager.getCache(CacheConstants.X_PAN_CACHE_NAME);
    }

}
