package com.xiaoxin.pan.storge.engine.core.context;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 删除物理文件的上下文实体信息
 */
@Data
public class DeleteFileContext implements Serializable {

    private static final long serialVersionUID = 7639667395701975450L;

    /**
     * 要删除的物理文件路径的集合
     */
    private List<String> realFilePathList;
}
