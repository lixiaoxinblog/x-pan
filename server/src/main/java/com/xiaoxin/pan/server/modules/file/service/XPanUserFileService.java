package com.xiaoxin.pan.server.modules.file.service;

import com.xiaoxin.pan.server.modules.file.context.CreateFolderContext;
import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author xiaoxin
 * @description 针对表【x_pan_user_file(用户文件信息表)】的数据库操作Service
 * @createDate 2024-07-26 12:48:16
 */
public interface XPanUserFileService extends IService<XPanUserFile> {
    public Long createFolder(CreateFolderContext createFolderContext);
}
