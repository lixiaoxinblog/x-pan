package com.xiaoxin.pan.server.modules.file.enmus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件夹表示枚举类
 */
@AllArgsConstructor
@Getter
public enum FolderFlagEnum {

    /**
     * 非文件夹
     */
    NO(0),
    /**
     * 是文件夹
     */
    YES(1);

    private Integer code;

}
