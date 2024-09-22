package com.xiaoxin.pan.server.modules.file.context;

import com.xiaoxin.pan.server.modules.file.entity.XPanFile;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * 保存单文件的上下文实体
 */
@Data
public class FileSaveContext implements Serializable {

    private static final long serialVersionUID = -5517063544206664471L;
    /**
     * 文件名称
     */
    private String filename;

    /**
     * 文件唯一标识
     */
    private String identifier;

    /**
     * 文件大小
     */
    private Long totalSize;

    /**
     * 要上传的文件实体
     */
    private MultipartFile file;

    /**
     * 当前登录的用户ID
     */
    private Long userId;

    /**
     * 实体文件记录
     */
    private XPanFile record;

    /**
     * 文件上传的物理路径
     */
    private String realPath;

}
