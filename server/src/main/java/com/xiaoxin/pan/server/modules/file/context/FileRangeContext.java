package com.xiaoxin.pan.server.modules.file.context;

import lombok.Data;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

@Data
public class FileRangeContext implements Serializable {

    private static final long serialVersionUID = -5096152372793856485L;
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

    /**
     * 当前分片
     */
    private String range;

    private Long start;
    private Long end;
}
