package com.xiaoxin.pan.server.modules.share.service;

import com.xiaoxin.pan.server.modules.share.context.SaveShareFilesContext;
import com.xiaoxin.pan.server.modules.share.entity.XPanShareFile;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author xiaoxin
* @description 针对表【x_pan_share_file(用户分享文件表)】的数据库操作Service
* @createDate 2024-07-26 12:50:32
*/
public interface XPanShareFileService extends IService<XPanShareFile> {

    /**
     * 保存分享的文件的对应关系
     * @param saveShareFilesContext
     */
    void saveShareFiles(SaveShareFilesContext saveShareFilesContext);
}
