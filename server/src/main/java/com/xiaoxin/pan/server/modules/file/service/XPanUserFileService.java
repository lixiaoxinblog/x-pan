package com.xiaoxin.pan.server.modules.file.service;

import com.xiaoxin.pan.server.modules.file.context.CreateFolderContext;
import com.xiaoxin.pan.server.modules.file.context.DeleteFileContext;
import com.xiaoxin.pan.server.modules.file.context.QueryFileListContext;
import com.xiaoxin.pan.server.modules.file.context.UpdateFilenameContext;
import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.baomidou.mybatisplus.extension.service.IService;
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
}
