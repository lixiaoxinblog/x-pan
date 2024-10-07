package com.xiaoxin.pan.server.modules.share.service.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoxin.pan.server.common.cache.AbstractManualCacheService;
import com.xiaoxin.pan.server.modules.share.entity.XPanShare;
import com.xiaoxin.pan.server.modules.share.mapper.XPanShareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 手动缓存实现分享业务的查询等操作
 */
@Component(value = "shareManualCacheService")
public class ShareCacheService extends AbstractManualCacheService<XPanShare> {

    @Autowired
    private XPanShareMapper xPanShareMapper;

    @Override
    protected BaseMapper<XPanShare> getMapper() {
        return xPanShareMapper;
    }

    /**
     * 获取缓存key的模板信息
     * @return
     */
    @Override
    public String getKeyFormat() {
        return "SHARE:ID:%s";
    }
}
