package com.xiaoxin.pan.server.common.cache;


import java.io.Serializable;

/**
 * 支持业务缓冲的顶级Service顶级接口
 *
 */
public interface CacheService<V> {


    /**
     * 根据ID插叙实体
     * @param id
     * @return
     */
    public V getById(Serializable id);

    /**
     * 根据ID更新实体
     * @param id
     * @param entity
     * @return
     */
    public boolean updateById(Serializable id, V entity);

    /**
     * 根据ID删除实体
     * @param id
     * @return
     */
    public boolean removeById(Serializable id);


}
