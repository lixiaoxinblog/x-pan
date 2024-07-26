package com.xiaoxin.pan.server.modules.log.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxin.pan.server.modules.log.entity.XPanErrorLog;
import com.xiaoxin.pan.server.modules.log.service.XPanErrorLogService;
import com.xiaoxin.pan.server.modules.log.mapper.XPanErrorLogMapper;
import org.springframework.stereotype.Service;

/**
* @author xiaoxin
* @description 针对表【x_pan_error_log(错误日志表)】的数据库操作Service实现
* @createDate 2024-07-26 12:49:23
*/
@Service
public class XPanErrorLogServiceImpl extends ServiceImpl<XPanErrorLogMapper, XPanErrorLog>
    implements XPanErrorLogService{

}




