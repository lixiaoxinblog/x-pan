package com.xiaoxin.pan.storge.engine.core.context;

import lombok.Data;

import java.io.OutputStream;
import java.io.Serializable;

@Data
public class ReadRangeFileContext implements Serializable {

    private static final long serialVersionUID = 7311361670119670270L;
    /**
     * 文件的真实存储路径
     */
    private String realPath;

    /**
     * 文件的输出流
     */
    private OutputStream outputStream;

    /**
     * 文件开始位置
     */
    private Long start;

    /**
     * 文件结束位置
     */
    private Long end;
}
