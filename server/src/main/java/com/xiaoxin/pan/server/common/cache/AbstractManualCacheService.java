package com.xiaoxin.pan.server.common.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.google.common.collect.Lists;
import com.xiaoxin.pan.cache.core.constanst.CacheConstants;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 手动处理缓存的公用顶级父类
 */
public abstract class AbstractManualCacheService<V> implements ManualCacheService<V> {

    @Autowired(required = false)
    private CacheManager cacheManager;

    // 本地锁对象
    private final Object lock = new Object();

    protected abstract BaseMapper<V> getMapper();

    /**
     * 根据ID查询缓存实体
     * 1.查询缓存，如果命中直接返回
     * 2.没有命中，查询数据库
     * 3.将查询到的数据放入缓存
     *
     * @param id
     * @return
     */
    @Override
    public V getById(Serializable id) {
        if (Objects.isNull(id)) {
            return null;
        }
        V result = getByCache(id);
        if (Objects.nonNull(result)) {
            return result;
        }
        //使用锁机制防止缓存击穿问题
        synchronized (lock) {
            result = getByCache(id);
            if (Objects.nonNull(result)) {
                return result;
            }
            result = getByDB(id);
            if (Objects.nonNull(result)) {
                putCache(id, result);
            }
        }
        return result;
    }

    /**
     * 根据ID移除缓存数据
     *
     * @param id
     * @param entity
     * @return
     */
    @Override
    public boolean updateById(Serializable id, V entity) {
        int row = getMapper().updateById(entity);
        removeCache(id);
        return row == 1;
    }

    /**
     * 根据ID移除缓存数据
     *
     * @param id
     * @return
     */
    @Override
    public boolean removeById(Serializable id) {
        int row = getMapper().deleteById(id);
        removeCache(id);
        return row == 1;
    }

    /**
     * 批量查询缓存数据
     *
     * @param ids
     * @return
     */
    @Override
    public List<V> getByIds(Collection<? extends Serializable> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        return ids
                .stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    /**
     * 批量更新缓存数据
     * @param eneityMap
     * @return
     */
    @Override
    public boolean updateByIds(Map<? extends Serializable, V> eneityMap) {
        if (CollectionUtils.isEmpty(eneityMap)){
            return false;
        }
        for (Map.Entry<? extends Serializable, V> entry : eneityMap.entrySet()) {
            if (!updateById(entry.getKey(),entry.getValue())){
                return false;
            }
        }
        return true;
    }

    /**
     * 批量删除缓存数据
     * @param ids
     * @return
     */
    @Override
    public boolean removeByIds(Collection<? extends Serializable> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return false;
        }
        return ids.stream().allMatch(this::removeById);
    }

    @Override
    public Cache getCache() {
        if (Objects.isNull(cacheManager)) {
            throw new XPanBusinessException("this cache manager is empty !");
        }
        return cacheManager.getCache(CacheConstants.X_PAN_CACHE_NAME);
    }


    /**
     * 将查询到的实体放入缓存
     *
     * @param id
     * @param entity
     */
    private void putCache(Serializable id, V entity) {
        String cacheKey = getCacheKey(id);
        Cache cache = getCache();
        if (Objects.isNull(cache)) {
            return;
        }
        if (Objects.isNull(entity)) {
            return;
        }
        cache.put(cacheKey, entity);
    }

    /**
     * 从数据库中查询实体
     *
     * @param id
     * @return
     */
    private V getByDB(Serializable id) {
        return getMapper().selectById(id);
    }

    /**
     * 根据ID查询缓存实体
     *
     * @param id
     * @return
     */
    private V getByCache(Serializable id) {
        String key = getCacheKey(id);
        Cache cache = getCache();
        if (Objects.isNull(cache)) {
            return null;
        }
        Cache.ValueWrapper valueWrapper = cache.get(key);
        if (Objects.isNull(valueWrapper)){
            return null;
        }
        return (V) valueWrapper.get();
    }

    /**
     * 获取缓存的key
     *
     * @param id
     * @return
     */
    protected String getCacheKey(Serializable id) {
        return String.format(getKeyFormat(), id);
    }

    /**
     * 移除缓存实体信息
     *
     * @param id
     */
    private void removeCache(Serializable id) {
        String cacheKey = getCacheKey(id);
        Cache cache = getCache();
        if (Objects.isNull(cache)) {
            return;
        }
        cache.evict(cacheKey);
    }
}
