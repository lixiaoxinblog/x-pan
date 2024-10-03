package com.xiaoxin.pan.server.modules.share.service;

import com.xiaoxin.pan.server.modules.share.context.CancelShareContext;
import com.xiaoxin.pan.server.modules.share.context.CreateShareUrlContext;
import com.xiaoxin.pan.server.modules.share.context.QueryShareListContext;
import com.xiaoxin.pan.server.modules.share.entity.XPanShare;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoxin.pan.server.modules.share.vo.XPanShareUrlListVO;
import com.xiaoxin.pan.server.modules.share.vo.XPanShareUrlVO;

import java.util.List;

/**
* @author xiaoxin
* @description 针对表【x_pan_share(用户分享表)】的数据库操作Service
* @createDate 2024-07-26 12:50:31
*/
public interface XPanShareService extends IService<XPanShare> {

    /**
     * 创建分享链接
     * @param shareUrlPO2CreateShareUrlContext
     * @return
     */
    XPanShareUrlVO create(CreateShareUrlContext shareUrlPO2CreateShareUrlContext);

    /**
     * 查询用户的分享列表
     *
     * @param queryShareListContext
     * @return
     */
    List<XPanShareUrlListVO> getShares(QueryShareListContext queryShareListContext);

    /**
     * 取消分享链接
     *
     * @param cancelShareContext
     */
    void cancelShare(CancelShareContext cancelShareContext);
}
