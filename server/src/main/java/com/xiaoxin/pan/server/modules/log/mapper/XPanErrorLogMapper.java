package com.xiaoxin.pan.server.modules.log.mapper;

import com.xiaoxin.pan.server.modules.log.entity.XPanErrorLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author xiaoxin
* @description 针对表【x_pan_error_log(错误日志表)】的数据库操作Mapper
* @createDate 2024-07-26 12:49:23
* @Entity com.xiaoxin.pan.server.modules.log.entity.XPanErrorLog
*/
@Mapper
public interface XPanErrorLogMapper extends BaseMapper<XPanErrorLog> {

}




