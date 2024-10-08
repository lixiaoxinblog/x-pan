package com.xiaoxin.pan.bloom.filter.local;

import com.xiaoxin.pan.bloom.filter.BloomFilter;
import com.xiaoxin.pan.bloom.filter.BloomFilterManager;
import com.xiaoxin.pan.bloom.filter.local.enmus.FunnelType;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地布隆过滤器管理器
 * InitializingBean在spring初始化过程中发现我们的javabean继承了InitializingBean会在数据装载之后调用afterPropertiesSet()方法。
 */
@Component
public class LocalBloomFilterManager implements BloomFilterManager, InitializingBean {

    @Autowired
    private LocalBloomFilterConfig config;

    /**
     * 容器
     */
    private final Map<String,BloomFilter> bloomFilterContainer = new ConcurrentHashMap<>();

    /**
     * 获取布隆过滤器
     *
     * @return
     */
    @Override
    public BloomFilter getBloomFilter(String name) {
        return bloomFilterContainer.get(name);
    }

    /**
     * 获取布隆过滤器名称
     *
     * @return
     */
    @Override
    public Collection<String> getFilterNames() {
        return bloomFilterContainer.keySet();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<LocalBloomFilterConfigItem> items = config.getItems();
        if (CollectionUtils.isNotEmpty(items)){
            items.forEach(item ->{
                String funnelTypeName = item.getFunnelTypeName();
                try{
                    FunnelType funnelType = FunnelType.valueOf(funnelTypeName);
                    if (Objects.nonNull(funnelType)){
                        bloomFilterContainer
                                .putIfAbsent(item.getName(),
                                        new LocalBloomFilter(item.getExpectedInsertions(),
                                                item.getFpp(),funnelType.getFunnel())
                                );
                    }
                }catch (Exception e){

                }
            });
        }
    }
}
