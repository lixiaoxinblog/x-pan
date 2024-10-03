package com.xiaoxin.pan.server.modules.share.context;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SaveShareFilesContext implements Serializable {
    private static final long serialVersionUID = 3133956554003981513L;

    /**
     * 分享的ID
     */
    private Long shareId;

    /**
     * 分享对应的文件的ID集合
     */
    private List<Long> shareFileIdList;

    /**
     * 当前登录的用户ID
     */
    private Long userId;
}
