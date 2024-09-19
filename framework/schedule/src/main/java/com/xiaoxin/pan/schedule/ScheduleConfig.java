package com.xiaoxin.pan.schedule;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 定时模块配置类
 * 配置定时器执行器
 */
@SpringBootConfiguration
public class ScheduleConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }
}
