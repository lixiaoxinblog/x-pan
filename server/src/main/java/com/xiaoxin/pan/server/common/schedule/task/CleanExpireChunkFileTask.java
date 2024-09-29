package com.xiaoxin.pan.server.common.schedule.task;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.schedule.ScheduleTask;
import com.xiaoxin.pan.server.common.envent.log.ErrorLogEvent;
import com.xiaoxin.pan.server.modules.file.entity.XPanFileChunk;
import com.xiaoxin.pan.server.modules.file.service.XPanFileChunkService;
import com.xiaoxin.pan.server.modules.file.service.XPanFileService;
import com.xiaoxin.pan.storge.engine.core.StorageEngine;
import com.xiaoxin.pan.storge.engine.core.context.DeleteFileContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Wrapper;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 过期分片清理任务
 */
@Component
@Slf4j
public class CleanExpireChunkFileTask implements ScheduleTask, ApplicationContextAware {

    /**
     * 每次滚动查询的大小
     */
    private static final Long BATCH_SIZE = 500L;

    @Autowired
    private XPanFileChunkService xPanFileChunkService;

    private ApplicationContext applicationContext;

    @Autowired
    private StorageEngine storageEngine;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

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
        log.info("{} start clean expire chunk file...", getName());
        List<XPanFileChunk> expireFileChunkRecords;
        Long scrollPointer = 1L;
        do {
            expireFileChunkRecords = scrollQueryExpireFileChunkRecords(scrollPointer);
            if (CollectionUtils.isNotEmpty(expireFileChunkRecords)) {
                deleteRealChunkFiles(expireFileChunkRecords);
                List<Long> idList = deleteChunkFileRecords(expireFileChunkRecords);
                scrollPointer = Collections.max(idList);
            }
        } while (CollectionUtils.isNotEmpty(expireFileChunkRecords));
        log.info("{} start clean expire chunk file...", getName());
    }

    /**
     * 删除过期文件分片记录
     *
     * @param expireFileChunkRecords
     * @return
     */
    private List<Long> deleteChunkFileRecords(List<XPanFileChunk> expireFileChunkRecords) {
        List<Long> ids = expireFileChunkRecords.stream()
                .map(XPanFileChunk::getId)
                .collect(Collectors.toList());
        xPanFileChunkService.removeByIds(ids);
        return ids;
    }

    /**
     * 物理删除过期的文件分片文件实体
     *
     * @param expireFileChunkRecords
     */
    private void deleteRealChunkFiles(List<XPanFileChunk> expireFileChunkRecords) {
        DeleteFileContext deleteFileContext = new DeleteFileContext();
        List<String> realPaths = expireFileChunkRecords.stream()
                .map(XPanFileChunk::getRealPath)
                .collect(Collectors.toList());
        deleteFileContext.setRealFilePathList(realPaths);
        try {
            storageEngine.delete(deleteFileContext);
        } catch (IOException e) {
            saveErrorLog(realPaths);
        }
    }

    /**
     * 保存错误日志
     *
     * @param realPaths
     */
    private void saveErrorLog(List<String> realPaths) {
        ErrorLogEvent event = new ErrorLogEvent(this, "文件物理删除失败，请手动执行文件删除！文件路径为：" +
                JSON.toJSONString(realPaths), XPanConstants.ZERO_LONG);
        applicationContext.publishEvent(event);
    }

    /**
     * 滚动查询过期的文件分片记录
     *
     * @param scrollPointer
     * @return
     */
    private List<XPanFileChunk> scrollQueryExpireFileChunkRecords(Long scrollPointer) {
        LambdaQueryWrapper<XPanFileChunk> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.le(XPanFileChunk::getExpirationTime, new Date());
        queryWrapper.ge(XPanFileChunk::getId, scrollPointer);
        queryWrapper.last("limit " + BATCH_SIZE);
        return xPanFileChunkService.list(queryWrapper);
    }
}
