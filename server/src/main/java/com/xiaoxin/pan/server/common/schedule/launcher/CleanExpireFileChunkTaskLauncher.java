package com.xiaoxin.pan.server.common.schedule.launcher;


import com.xiaoxin.pan.schedule.ScheduleManager;
import com.xiaoxin.pan.server.common.schedule.task.CleanExpireChunkFileTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 定时清理过期的文件分片任务触发器
 */
@Slf4j
@Component
public class CleanExpireFileChunkTaskLauncher implements CommandLineRunner {

    @Autowired
    private CleanExpireChunkFileTask cleanExpireChunkFileTask;

    @Autowired
    private ScheduleManager scheduleManager;

//    private final static String CRON = "0/5 * * * * ?";
    private final static String CRON = "1 0 0 * * ? ";

    @Override
    public void run(String... args) throws Exception {
        scheduleManager.startTask(cleanExpireChunkFileTask,CRON);
    }
}
