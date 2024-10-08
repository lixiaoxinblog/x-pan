package com.xiaoxin.pan.bloom.filter.local;

import com.xiaoxin.pan.bloom.filter.local.enmus.FunnelType;
import lombok.Data;

/**
 * 本地布隆过滤器单体配置项
 */
@Data
public class LocalBloomFilterConfigItem {

    /**
     * 布隆过滤器名称
     */
    private String name;

    /**
     * 数据通道名称
     */
    private String funnelTypeName = FunnelType.LONG.name();

    /**
     * 数组的长度
     */
    private long expectedInsertions = 1000000L;
    /**
     * 误判率
     */
    private double fpp = 0.01D;

}
