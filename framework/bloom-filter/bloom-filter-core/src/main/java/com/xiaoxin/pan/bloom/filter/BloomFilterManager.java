package com.xiaoxin.pan.bloom.filter;

import java.util.Collection;

/**
 * 布隆过滤器管理器
 */
public interface BloomFilterManager {
    /**
     * 获取布隆过滤器
     * @return
     */
    BloomFilter getBloomFilter(String name);

    /**
     * 获取布隆过滤器名称
     * @return
     */
    Collection<String> getFilterNames();

}
