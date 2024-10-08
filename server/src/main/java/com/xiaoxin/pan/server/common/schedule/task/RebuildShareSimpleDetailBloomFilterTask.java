package com.xiaoxin.pan.server.common.schedule.task;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaoxin.pan.bloom.filter.BloomFilter;
import com.xiaoxin.pan.bloom.filter.BloomFilterManager;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.schedule.ScheduleTask;
import com.xiaoxin.pan.server.common.envent.log.ErrorLogEvent;
import com.xiaoxin.pan.server.modules.file.entity.XPanFileChunk;
import com.xiaoxin.pan.server.modules.share.service.XPanShareService;
import com.xiaoxin.pan.storge.engine.core.context.DeleteFileContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 过期分片清理任务
 */
@Component
@Slf4j
public class RebuildShareSimpleDetailBloomFilterTask implements ScheduleTask{
    @Autowired
    private BloomFilterManager manager;

    private static final String BLOOM_FILTER_NAME = "SHARE_SIMPLE_DETAIL";

    @Autowired
    private XPanShareService xPanShareService;

    /**
     * 获取定时任务名称
     *
     * @return
     */
    @Override
    public String getName() {
        return "ClearExpireChunkTask";
    }


    /**
     * 执行清理任务
     * 1、滚动查询过期的文件分片
     * 2、删除物理文件（委托文件存储引擎去实现）
     * 3、删除过期文件分片的记录信息
     * 4、重置上次查询的最大文件分片记录ID，继续滚动查询
     */
    @Override
    public void run() {
        log.info("start rebuild ShareSimpleDetailBloomFilter...");
        BloomFilter bloomFilter = manager.getBloomFilter(BLOOM_FILTER_NAME);
        if (Objects.isNull(bloomFilter)) {
            log.info("the bloomFilter named {} is null ,give up rebuild ...", BLOOM_FILTER_NAME);
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

        log.info("finish rebuild ShareSimpleDetailBloomFilter, total set item count {}...", atomicLong.get());
    }
}
