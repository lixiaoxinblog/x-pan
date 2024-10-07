package com.xiaoxin.pan.server.modules.share.context;

import com.xiaoxin.pan.server.modules.share.entity.XPanShare;
import com.xiaoxin.pan.server.modules.share.vo.ShareDetailVO;
import com.xiaoxin.pan.server.modules.share.vo.ShareUserInfoVO;
import lombok.Data;

import java.io.Serializable;

@Data
public class QueryShareDetailContext implements Serializable {
    private static final long serialVersionUID = 1530590680883115638L;

    /**
     * 对应的分享ID
     */
    private Long shareId;

    /**
     * 分享实体
     */
    private XPanShare record;

    /**
     * 分享详情的VO对象
     */
    private ShareDetailVO vo;

}
