package com.xiaoxin.pan.bloom.filter.local;

import com.google.common.hash.Funnel;
import com.xiaoxin.pan.bloom.filter.BloomFilter;

/**
 * 本地布隆过滤器实现
 *
 * @param <T>
 */
public class LocalBloomFilter<T> implements BloomFilter<T> {
    /**
     * 布隆过滤器
     */
    private com.google.common.hash.BloomFilter<T> delegate;
    /**
     * 数据类型通道
     */
    private Funnel funnel;
    /**
     * 数组的长度
     */
    private long expectedInsertions;
    /**
     * 误判率
     */
    private double fpp;

    public LocalBloomFilter(long expectedInsertions, double fpp, Funnel funnel) {
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
        this.funnel = funnel;
        this.delegate = com.google.common.hash.BloomFilter.create(funnel, expectedInsertions, fpp);
    }

    /**
     * 添加元素
     *
     * @param objcet
     */
    @Override
    public boolean put(T objcet) {
        return delegate.put(objcet);
    }

    /**
     * 判断可能存在的元素
     *
     * @param object
     * @return
     */
    @Override
    public boolean mightContain(T object) {
        return delegate.mightContain(object);
    }

    /**
     * 清空元素
     *
     * @return
     */
    @Override
    public void clear() {
        this.delegate = com.google.common.hash.BloomFilter.create(funnel,expectedInsertions,fpp);
    }

}
