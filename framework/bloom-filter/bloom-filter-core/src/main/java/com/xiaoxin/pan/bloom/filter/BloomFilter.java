package com.xiaoxin.pan.bloom.filter;

/**
 * 布隆过滤器顶级接口
 */
public interface BloomFilter<T> {

    /**
     * 添加元素
     * @param objcet
     */
    public boolean put(T objcet);

    /**
     * 判断可能存在的元素
     * @param object
     * @return
     */
    boolean mightContain(T object);

    /**
     * 清空元素
     * @return
     */
    void clear();

}
