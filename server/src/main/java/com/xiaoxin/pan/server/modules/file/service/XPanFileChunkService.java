package com.xiaoxin.pan.server.modules.file.service;

import com.xiaoxin.pan.server.modules.file.context.FileChunkSaveContext;
import com.xiaoxin.pan.server.modules.file.entity.XPanFileChunk;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author xiaoxin
* @description 针对表【x_pan_file_chunk(文件分片信息表)】的数据库操作Service
* @createDate 2024-07-26 12:48:15
*/
public interface XPanFileChunkService extends IService<XPanFileChunk> {

    void saveChunkFile(FileChunkSaveContext fileChunkSaveContext);
}
