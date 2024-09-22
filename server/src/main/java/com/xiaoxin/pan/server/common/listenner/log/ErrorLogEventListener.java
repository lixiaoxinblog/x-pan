package com.xiaoxin.pan.server.common.listenner.log;

import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.common.envent.log.ErrorLogEvent;
import com.xiaoxin.pan.server.modules.log.entity.XPanErrorLog;
import com.xiaoxin.pan.server.modules.log.service.XPanErrorLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ErrorLogEventListener {
    @Autowired
    private XPanErrorLogService xPanErrorLogService;

    @EventListener(ErrorLogEvent.class)
    @Async(value = "eventListenerTaskExecutor")
    public void saveErrorLog(ErrorLogEvent errorLogEvent){
        XPanErrorLog xPanErrorLog = new XPanErrorLog();
        xPanErrorLog.setId(IdUtil.get());
        xPanErrorLog.setLogContent(errorLogEvent.getErrorMsg());
        xPanErrorLog.setLogStatus(0);
        xPanErrorLog.setCreateUser(errorLogEvent.getUserId());
        xPanErrorLog.setCreateTime(new Date());
        xPanErrorLog.setUpdateUser(errorLogEvent.getUserId());
        xPanErrorLog.setUpdateTime(new Date());
        xPanErrorLogService.save(xPanErrorLog);
    }

}
