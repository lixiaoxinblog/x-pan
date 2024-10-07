package com.xiaoxin.pan.server.common.cache;


import org.springframework.cache.Cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 手动缓存处理Service顶级接口
 * @param <V>
 */
public interface ManualCacheService<V> extends CacheService<V>{

    /**
     * 批量获取缓存
     * @param ids
     * @return
     */
    List<V> getByIds(Collection<? extends Serializable> ids);

    /**
     * 批量更新缓存
     * @param eneityMap
     * @return
     */
    boolean updateByIds(Map<?extends Serializable ,V> eneityMap);

    /**
     * 批量删除缓存
     * @param ids
     * @return
     */
    boolean removeByIds(Collection<? extends Serializable> ids);

    /**
     * 获取缓存key格式
     * @return
     */
    String getKeyFormat();

    /**
     * 获取缓存对象实体
     * @return
     */
    Cache getCache();

}
