package com.xiaoxin.pan.server.common.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 布隆过滤器拦截器
 */
public interface BloomFilterInterceptor extends HandlerInterceptor {

    /**
     * 拦截器名称
     * @return
     */
    public String getName();

    /**
     * 要拦截的URI集合
     * @return
     */
    String[] getPathPatterns();

    /**
     * 要过滤的拦截的集合
     * @return
     */
    String[] getExcludePatterns();

}
