package com.xiaoxin.pan.server.modules.file.mapper;

import com.xiaoxin.pan.server.modules.file.entity.XPanFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author xiaoxin
* @description 针对表【x_pan_file(物理文件信息表)】的数据库操作Mapper
* @createDate 2024-07-26 12:48:16
* @Entity com.xiaoxin.pan.server.modules.file.entity.XPanFile
*/
@Mapper
public interface XPanFileMapper extends BaseMapper<XPanFile> {

}




