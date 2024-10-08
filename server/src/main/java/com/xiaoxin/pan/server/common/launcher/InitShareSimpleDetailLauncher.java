package com.xiaoxin.pan.server.common.launcher;

import com.xiaoxin.pan.bloom.filter.BloomFilter;
import com.xiaoxin.pan.bloom.filter.BloomFilterManager;
import com.xiaoxin.pan.server.modules.share.service.XPanShareService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 简单分享详情布隆过滤器初始化器
 */
@Component
@Slf4j
public class InitShareSimpleDetailLauncher implements CommandLineRunner {
    @Autowired
    private BloomFilterManager manager;

    private static final String BLOOM_FILTER_NAME = "SHARE_SIMPLE_DETAIL";

    @Autowired
    private XPanShareService xPanShareService;

    @Override
    public void run(String... args) throws Exception {
        log.info("start init ShareSimpleDetailBloomFilter...");
        BloomFilter bloomFilter = manager.getBloomFilter(BLOOM_FILTER_NAME);
        if (Objects.isNull(bloomFilter)) {
            log.info("the bloomFilter named {} is null ,give up init ...", BLOOM_FILTER_NAME);
            return;
        }
        bloomFilter.clear();

        long startId = 0L;
        long limit = 10000L;
        List<Long> shareIds = null;
        AtomicLong atomicLong = new AtomicLong();
        do {
            shareIds = xPanShareService.rollingQueryShareId(startId, limit);
            if (CollectionUtils.isNotEmpty(shareIds)) {
                shareIds.forEach(shareId -> {
                    bloomFilter.put(shareId);
                    atomicLong.incrementAndGet();
                });
                startId = shareIds.get(shareIds.size() - 1);
            }
        } while (CollectionUtils.isNotEmpty(shareIds));

        log.info("finish init ShareSimpleDetailBloomFilter, total set item count {}...", atomicLong.get());
    }
}
