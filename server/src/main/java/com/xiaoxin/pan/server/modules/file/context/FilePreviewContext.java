package com.xiaoxin.pan.server.modules.file.context;

import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * 文件预览的上下文实体对象
 */
@Data
public class FilePreviewContext implements Serializable {

    private static final long serialVersionUID = -8445560197374062497L;
    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 请求响应对象
     */
    private HttpServletResponse response;

    /**
     * 当前登录的用户ID
     */
    private Long userId;

}
