package com.xiaoxin.pan.server.modules.share.context;

import com.xiaoxin.pan.server.modules.share.entity.XPanShare;
import com.xiaoxin.pan.server.modules.share.vo.ShareSimpleDetailVO;
import lombok.Data;

import java.io.Serializable;

/**
 * 查询分享简单详情上下文实体信息
 */
@Data
public class QueryShareSimpleDetailContext implements Serializable {

    private static final long serialVersionUID = 1045511119228640519L;
    /**
     * 分享的ID
     */
    private Long shareId;

    /**
     * 分享对应的实体信息
     */
    private XPanShare record;

    /**
     * 简单分享详情的VO对象
     */
    private ShareSimpleDetailVO vo;

}
