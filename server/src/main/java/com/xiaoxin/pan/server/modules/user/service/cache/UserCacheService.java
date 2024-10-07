package com.xiaoxin.pan.server.modules.user.service.cache;

import com.xiaoxin.pan.cache.core.constanst.CacheConstants;
import com.xiaoxin.pan.server.common.cache.AnnotationCacheService;
import com.xiaoxin.pan.server.modules.user.entity.XPanUser;
import com.xiaoxin.pan.server.modules.user.mapper.XPanUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 用户模块缓存业务处理类
 */
@Component(value = "userAnnotationCacheService")
public class UserCacheService implements AnnotationCacheService<XPanUser> {

    @Autowired
    private XPanUserMapper xPanUserMapper;

    @Override
    @Cacheable(cacheNames = CacheConstants.X_PAN_CACHE_NAME,keyGenerator = "userIdKeyGenerator",sync = true)
    public XPanUser getById(Serializable id) {
        return xPanUserMapper.selectById(id);
    }

    @Override
    @CachePut(cacheNames = CacheConstants.X_PAN_CACHE_NAME,keyGenerator = "userIdKeyGenerator")
    public boolean updateById(Serializable id, XPanUser entity) {
        return xPanUserMapper.updateById(entity) == 1;
    }

    @Override
    @CacheEvict(cacheNames = CacheConstants.X_PAN_CACHE_NAME,keyGenerator = "userIdKeyGenerator")
    public boolean removeById(Serializable id) {
        return xPanUserMapper.deleteById(id) == 1;
    }
}
