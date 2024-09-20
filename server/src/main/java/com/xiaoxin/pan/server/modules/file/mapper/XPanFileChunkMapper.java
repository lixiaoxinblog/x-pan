package com.xiaoxin.pan.server.modules.file.mapper;

import com.xiaoxin.pan.server.modules.file.entity.XPanFileChunk;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author xiaoxin
* @description 针对表【x_pan_file_chunk(文件分片信息表)】的数据库操作Mapper
* @createDate 2024-07-26 12:48:15
* @Entity com.xiaoxin.pan.server.modules.file.entity.XPanFileChunk
*/
@Mapper
public interface XPanFileChunkMapper extends BaseMapper<XPanFileChunk> {

}




