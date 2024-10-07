package com.xiaoxin.pan.server.modules.share.context;

import com.xiaoxin.pan.server.modules.share.entity.XPanShare;
import lombok.Data;

import java.io.Serializable;

/**
 * 校验分享码上下文实体对象
 */
@Data
public class CheckShareCodeContext implements Serializable {

    private static final long serialVersionUID = 7206958086528552983L;
    /**
     * 分享ID
     */
    private Long shareId;

    /**
     * 分享码
     */
    private String shareCode;

    /**
     * 对应的分享实体
     */
    private XPanShare record;

}
