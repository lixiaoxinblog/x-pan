package com.xiaoxin.pan.server.modules.file.service;

import com.xiaoxin.pan.server.modules.file.context.*;
import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoxin.pan.server.modules.file.vo.FileChunkUploadVO;
import com.xiaoxin.pan.server.modules.file.vo.UploadedChunksVO;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;

import java.util.List;

/**
 * @author xiaoxin
 * @description 针对表【x_pan_user_file(用户文件信息表)】的数据库操作Service
 * @createDate 2024-07-26 12:48:16
 */
public interface XPanUserFileService extends IService<XPanUserFile> {
    public Long createFolder(CreateFolderContext createFolderContext);

    XPanUserFile getUserRootFile(Long userId);

    /**
     * 查询列表
     * @param queryFileListContext 查询上下文
     * @return 文件列表
     */
    List<XPanUserFileVO> getFileList(QueryFileListContext queryFileListContext);

    /**
     * 文件重命名
     * @param updateFilenameContext
     */
    void updateFileName(UpdateFilenameContext updateFilenameContext);

    /**
     * 批量删除文件
     * @param deleteFileContext
     */
    void deleteFile(DeleteFileContext deleteFileContext);

    /**
     * 文件秒传
     * @param uploadFileContext
     * @return
     */
    boolean secUpload(UploadFileContext uploadFileContext);

    /**
     * 单文件上传
     * @param fileUploadContext
     */
    void upload(FileUploadContext fileUploadContext);

    /**
     * 文件分片上传
     * @param fileChunkUploadContext
     * @return
     */
    FileChunkUploadVO chunkUpload(FileChunkUploadContext fileChunkUploadContext);

    /**
     * 获取已经上传的分片
     * @param queryUploadedChunksContext
     * @return
     */
    UploadedChunksVO getUploadedChunks(QueryUploadedChunksContext queryUploadedChunksContext);

    /**
     * 文件分片合并
     * @param fileChunkMergeContext
     */
    void mergeFile(FileChunkMergeContext fileChunkMergeContext);

    /**
     * 文件下载
     * @param fileDownloadContext
     */
    void download(FileDownloadContext fileDownloadContext);
}
