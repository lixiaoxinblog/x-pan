package com.xiaoxin.pan.server.modules.share.converter;

import com.xiaoxin.pan.server.modules.share.context.CreateShareUrlContext;
import com.xiaoxin.pan.server.modules.share.po.CreateShareUrlPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 分享模块实体转化工具类
 */
@Mapper(componentModel = "spring")
public interface ShareConverter {
    @Mapping(target = "userId",expression = "java(com.xiaoxin.pan.server.common.utils.UserIdUtil.get())")
    CreateShareUrlContext createShareUrlPO2CreateShareUrlContext(CreateShareUrlPO createShareUrlPO);
}
