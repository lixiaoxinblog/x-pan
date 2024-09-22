package com.xiaoxin.pan.server.modules.file.service;

import com.xiaoxin.pan.server.modules.file.context.QueryFileListContext;
import com.xiaoxin.pan.server.modules.file.context.QueryRealFileListContext;
import com.xiaoxin.pan.server.modules.file.entity.XPanFile;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;

import java.util.List;

/**
* @author xiaoxin
* @description 针对表【x_pan_file(物理文件信息表)】的数据库操作Service
* @createDate 2024-07-26 12:48:16
*/
public interface XPanFileService extends IService<XPanFile> {

    List<XPanFile> getFileList(QueryRealFileListContext queryRealFileListContext);
}
