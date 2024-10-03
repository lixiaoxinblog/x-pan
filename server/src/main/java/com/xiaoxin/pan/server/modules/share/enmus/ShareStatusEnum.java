package com.xiaoxin.pan.server.modules.share.enmus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分享状态枚举类
 */
@AllArgsConstructor
@Getter
public enum ShareStatusEnum {

    NORMAL(0, "正常状态"),
    FILE_DELETED(1, "有文件被删除");

    private final Integer code;

    private final String desc;
}
