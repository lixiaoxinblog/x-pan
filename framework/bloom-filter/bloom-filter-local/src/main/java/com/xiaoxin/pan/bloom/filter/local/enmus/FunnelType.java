package com.xiaoxin.pan.bloom.filter.local.enmus;

import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.charset.StandardCharsets;

/**
 * 数据通道枚举类
 */
@Getter
@AllArgsConstructor
public enum FunnelType {

    /**
     * long数据通道
     */
    LONG(Funnels.longFunnel()),
    /**
     * int数据通道
     */
    INTEGER(Funnels.integerFunnel()),
    /**
     * string数据通道
     */
    STRING(Funnels.stringFunnel(StandardCharsets.UTF_8));
    private Funnel funnel;

}
