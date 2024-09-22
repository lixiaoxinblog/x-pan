package com.xiaoxin.pan.server.modules.file.context;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 文件秒传上下文对象
 */
@Data
@AllArgsConstructor
public class UploadFileContext {
    private static final long serialVersionUID = 865765374680289146L;

    /**
     * 文件的父ID
     */
    private Long parentId;

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 文件的唯一标识
     */
    private String identifier;

    /**
     * 当前登录用的ID
     */
    private Long userId;


}
