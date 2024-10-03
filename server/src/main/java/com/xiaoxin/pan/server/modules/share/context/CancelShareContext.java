package com.xiaoxin.pan.server.modules.share.context;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CancelShareContext implements Serializable {

    private static final long serialVersionUID = 9193285146301260325L;

    /**
     * 当前登录的用户ID
     */
    private Long userId;

    /**
     * 要取消的分享ID集合
     */
    private List<Long> shareIdList;
}
