package com.xiaoxin.pan.server.modules.share.enmus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * 分享类型枚举类
 */
@AllArgsConstructor
@Getter
public enum ShareTypeEnum {

    NEED_SHARE_CODE(0, "有提取码");
    private final Integer code;
    private final String desc;

}
