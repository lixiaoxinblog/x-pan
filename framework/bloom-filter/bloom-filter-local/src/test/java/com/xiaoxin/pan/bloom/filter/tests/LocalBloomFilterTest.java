package com.xiaoxin.pan.bloom.filter.tests;

import com.xiaoxin.pan.bloom.filter.BloomFilter;
import com.xiaoxin.pan.bloom.filter.local.LocalBloomFilterManager;
import com.xiaoxin.pan.core.constants.XPanConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = LocalBloomFilterTest.class)
@SpringBootApplication(scanBasePackages = XPanConstants.BASE_COMPONENT_SCAN_PATH + ".bloom.filter.local")
@Slf4j
public class LocalBloomFilterTest {

    @Autowired
    private LocalBloomFilterManager localBloomFilterManager;

    /**
     * 本地布隆过滤器测试
     */
    @Test
    public void localBloomFilterTest(){
        BloomFilter<Integer> bloomFilter = localBloomFilterManager.getBloomFilter("test");
        long failNum  = 0L;
        for (int i = 0; i < 1000000; i++) {
            bloomFilter.put(i);
        }
        for (int i = 1000000; i < 1100000; i++) {
            boolean result = bloomFilter.mightContain(i);
            if (result){
                failNum++;
            }
        }
        log.info("test num {} ,fail num {}",100000,failNum);
    }

}
