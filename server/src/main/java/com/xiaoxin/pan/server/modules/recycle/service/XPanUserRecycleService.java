package com.xiaoxin.pan.server.modules.recycle.service;

import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import com.xiaoxin.pan.server.modules.recycle.context.QueryRecycleFileListContext;
import com.xiaoxin.pan.server.modules.recycle.context.RestoreContext;

import java.util.List;

public interface XPanUserRecycleService {

    /**
     * 查询用户回收站文件列表
     * @param queryRecycleFileListContext
     * @return
     */
    List<XPanUserFileVO> list(QueryRecycleFileListContext queryRecycleFileListContext);

    /**
     * 批量还原回收站文件
     * @param restoreContext
     */
    void restore(RestoreContext restoreContext);
}
