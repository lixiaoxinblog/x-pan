package com.xiaoxin.pan.storge.engine.core;


import com.xiaoxin.pan.storge.engine.core.context.DeleteFileContext;
import com.xiaoxin.pan.storge.engine.core.context.StoreFileChunkContext;
import com.xiaoxin.pan.storge.engine.core.context.StoreFileContext;

import java.io.IOException;

/**
 * 文件存储引擎模块的顶级接口
 * 该接口定义所有需要向外暴露给业务层面的相关文件操作的功能
 * 业务方只能调用该接口的方法，而不能直接使用具体的实现方案去做业务调用
 */
public interface StorageEngine {


    /**
     * 存储物理文件
     *
     * @param storeFileContext
     * @throws IOException
     */
    void store(StoreFileContext storeFileContext) throws IOException;

    /**
     * 删除物理文件
     */
    void delete(DeleteFileContext deleteFileContext) throws IOException;

    /**
     * 文件分片上传
     * @param storeFileChunkContext
     * @throws IOException
     */
    void storeChunk(StoreFileChunkContext storeFileChunkContext) throws IOException;
}
