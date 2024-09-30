package com.xiaoxin.pan.server.modules.recycle.context;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 查询用户回收站文件列表上下文实体对象
 */
@Data
@ToString
public class QueryRecycleFileListContext implements Serializable {
    private static final long serialVersionUID = 7266377776873434330L;

    private Long userId;

}
