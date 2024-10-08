package com.xiaoxin.pan.server.common.interceptor;

import com.xiaoxin.pan.bloom.filter.BloomFilter;
import com.xiaoxin.pan.bloom.filter.BloomFilterManager;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.core.response.ResponseCode;
import com.xiaoxin.pan.core.utils.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 查询简单布隆过滤器拦截器
 */
@Component
@Slf4j
public class ShareSimpleDetailBloomInterceptor implements BloomFilterInterceptor{

    @Autowired
    private BloomFilterManager manager;

    private static final String BLOOM_FILTER_NAME  = "SHARE_SIMPLE_DETAIL";

    /**
     * 要过滤的拦截的集合
     *
     * @return
     */
    @Override
    public String[] getExcludePatterns() {
        return new String[0];
    }

    /**
     * 拦截器名称
     *
     * @return
     */
    @Override
    public String getName() {
        return "ShareSimpleDetailBloomInterceptor";
    }

    /**
     * 要拦截的URI集合
     *
     * @return
     */
    @Override
    public String[] getPathPatterns() {
        return ArrayUtils.toArray("/share/simple");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String encShareId = request.getParameter("shareId");
        if (StringUtils.isBlank(encShareId)){
            throw new XPanBusinessException("分享ID为空！");
        }
        BloomFilter bloomFilter = manager.getBloomFilter(BLOOM_FILTER_NAME);
        if (Objects.isNull(bloomFilter)){
            log.info("the bloom filter named {} is null,give up existence judgment ...",BLOOM_FILTER_NAME);
            return true;
        }
        Long decrypt = IdUtil.decrypt(encShareId);
        boolean mightContain = bloomFilter.mightContain(decrypt);
        if (mightContain){
            log.info("the bloom filter named {} judge shareId {} mightContain pass",BLOOM_FILTER_NAME,decrypt);
            return true;
        }
        log.info("the bloom filter named {} judge shareId {} mightContain fail",BLOOM_FILTER_NAME,decrypt);
        throw new XPanBusinessException(ResponseCode.SHARE_CANCELLED);
    }
}
