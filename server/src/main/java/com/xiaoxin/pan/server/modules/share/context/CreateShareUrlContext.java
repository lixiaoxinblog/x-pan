package com.xiaoxin.pan.server.modules.share.context;

import com.xiaoxin.pan.server.modules.share.entity.XPanShare;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class CreateShareUrlContext implements Serializable {
    private static final long serialVersionUID = 3426184950133039282L;
    /**
     * 分享的名称
     */
    private String shareName;

    /**
     * 分享的类型
     */
    private Integer shareType;

    /**
     * 分享的日期类型
     */
    private Integer shareDayType;

    /**
     * 该分项对应的文件ID集合
     */
    private List<Long> shareFileIdList;

    /**
     * 当前登录的用户ID
     */
    private Long userId;

    /**
     * 已经保存的分享实体信息
     */
    private XPanShare record;
}
