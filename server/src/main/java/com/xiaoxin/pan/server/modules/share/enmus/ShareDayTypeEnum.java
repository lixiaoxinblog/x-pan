package com.xiaoxin.pan.server.modules.share.enmus;

import com.xiaoxin.pan.core.constants.XPanConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 *分享日期类型枚举类
 */
@AllArgsConstructor
@Getter
public enum ShareDayTypeEnum {

    PERMANENT_VALIDITY(0, 0, "永久有效"),
    SEVEN_DAYS_VALIDITY(1, 7, "七天有效"),
    THIRTY_DAYS_VALIDITY(2, 30, "三十天有效");

    private final Integer code;

    private final Integer days;

    private final String desc;


    /**
     * 根据传过来的分享天数的code来获取对应的天数数值
     * @param code
     * @return
     */
    public static Integer getShareDayByCode(Integer code) {
        if (Objects.isNull(code)) {
            return XPanConstants.MINUS_ONE_INT;
        }
        for (ShareDayTypeEnum value : values()) {
            if (Objects.equals(value.getCode(), code)) {
                return value.getDays();
            }
        }
        return XPanConstants.MINUS_ONE_INT;
    }

}
