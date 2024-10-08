package com.xiaoxin.pan.server.modules.share.service;

import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import com.xiaoxin.pan.server.modules.share.context.*;
import com.xiaoxin.pan.server.modules.share.entity.XPanShare;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoxin.pan.server.modules.share.po.CheckShareCodePO;
import com.xiaoxin.pan.server.modules.share.vo.ShareDetailVO;
import com.xiaoxin.pan.server.modules.share.vo.ShareSimpleDetailVO;
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

    /**
     * 校验分享链接code
     * @param checkShareCodeContext
     * @return
     */
    String checkShareCode(CheckShareCodeContext checkShareCodeContext);

    /**
     * 分享详情
     * @param queryShareDetailContext
     * @return
     */
    ShareDetailVO detail(QueryShareDetailContext queryShareDetailContext);

    /**
     * 简单分享详情
     * @param context
     * @return
     */
    ShareSimpleDetailVO simpleDetail(QueryShareSimpleDetailContext context);

    /**
     * 获取下一级的文件列表
     *
     * @param queryChildFileListContext
     * @return
     */
    List<XPanUserFileVO> fileList(QueryChildFileListContext queryChildFileListContext);

    /**
     * 保存文件保存到我的网盘
     * @param context
     */
    void saveFiles(ShareSaveContext context);

    /**
     * 文件分享下载功能
     * @param shareFileDownloadContext
     */
    void download(ShareFileDownloadContext shareFileDownloadContext);

    /**
     * 滚动查询分享ID
     * @param startId
     * @param limit
     * @return
     */
    List<Long> rollingQueryShareId(long startId, long limit);
}
