package com.xiaoxin.pan.server.common.schedule.launcher;


import com.xiaoxin.pan.schedule.ScheduleManager;
import com.xiaoxin.pan.server.common.schedule.task.CleanExpireChunkFileTask;
import com.xiaoxin.pan.server.common.schedule.task.RebuildShareSimpleDetailBloomFilterTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 定时清理过期的文件分片任务触发器
 */
@Slf4j
@Component
public class RebuildShareSimpleDetailBloomFilterTaskLauncher implements CommandLineRunner {

    @Autowired
    private RebuildShareSimpleDetailBloomFilterTask task;

    @Autowired
    private ScheduleManager scheduleManager;

    /**
     * 每天凌晨0点0分1秒触发
     */
    private final static String CRON = "1 0 0 * * ? ";

    @Override
    public void run(String... args) throws Exception {
        scheduleManager.startTask(task,CRON);
    }
}
