package com.xiaoxin.pan.server.modules.share.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.common.config.PanServerConfig;
import com.xiaoxin.pan.server.modules.share.context.CancelShareContext;
import com.xiaoxin.pan.server.modules.share.context.CreateShareUrlContext;
import com.xiaoxin.pan.server.modules.share.context.QueryShareListContext;
import com.xiaoxin.pan.server.modules.share.context.SaveShareFilesContext;
import com.xiaoxin.pan.server.modules.share.enmus.ShareDayTypeEnum;
import com.xiaoxin.pan.server.modules.share.enmus.ShareStatusEnum;
import com.xiaoxin.pan.server.modules.share.entity.XPanShare;
import com.xiaoxin.pan.server.modules.share.entity.XPanShareFile;
import com.xiaoxin.pan.server.modules.share.service.XPanShareFileService;
import com.xiaoxin.pan.server.modules.share.service.XPanShareService;
import com.xiaoxin.pan.server.modules.share.mapper.XPanShareMapper;
import com.xiaoxin.pan.server.modules.share.vo.XPanShareUrlListVO;
import com.xiaoxin.pan.server.modules.share.vo.XPanShareUrlVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author xiaoxin
 * @description 针对表【x_pan_share(用户分享表)】的数据库操作Service实现
 * @createDate 2024-07-26 12:50:31
 */
@Service
public class XPanShareServiceImpl extends ServiceImpl<XPanShareMapper, XPanShare>
        implements XPanShareService {
    @Autowired
    private PanServerConfig panServerConfig;
    @Autowired
    private XPanShareFileService xPanShareFileService;

    /**
     * 创建分享链接
     * 拼装分享实体，保存到数据库
     * 保存分享和对应文件的关联关系
     * 拼装返回实体并返回
     *
     * @param shareUrlPO2CreateShareUrlContext
     * @return
     */
    @Transactional(rollbackFor = XPanBusinessException.class)
    @Override
    public XPanShareUrlVO create(CreateShareUrlContext shareUrlPO2CreateShareUrlContext) {
        saveShare(shareUrlPO2CreateShareUrlContext);
        saveShareFiles(shareUrlPO2CreateShareUrlContext);
        return assembleShareVO(shareUrlPO2CreateShareUrlContext);
    }

    /**
     * 查询用户的分享列表
     *
     * @param queryShareListContext
     * @return
     */
    @Override
    public List<XPanShareUrlListVO> getShares(QueryShareListContext queryShareListContext) {
        return baseMapper.selectShareVOListByUserId(queryShareListContext.getUserId());
    }


    /**
     * 取消分享链接
     * 1、校验用户操作权限
     * 2、删除对应的分享记录
     * 3、删除对应的分享文件关联关系记录
     *
     * @param cancelShareContext
     */
    @Transactional(rollbackFor = XPanBusinessException.class)
    @Override
    public void cancelShare(CancelShareContext cancelShareContext) {
        checkUserCancelSharePermission(cancelShareContext);
        doCancelShare(cancelShareContext);
        doCancelShareFiles(cancelShareContext);
    }

    /**
     * 取消文件和分享的关联关系数据
     *
     * @param cancelShareContext
     */
    private void doCancelShareFiles(CancelShareContext cancelShareContext) {
        LambdaQueryWrapper<XPanShareFile> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(XPanShareFile::getShareId,cancelShareContext.getShareIdList());
        queryWrapper.eq(XPanShareFile::getCreateUser,cancelShareContext.getUserId());
        if (!xPanShareFileService.remove(queryWrapper)) {
            throw new XPanBusinessException("取消分享失败");
        }

    }

    /**
     * 执行取消分享操作
     *
     * @param cancelShareContext
     */
    private void doCancelShare(CancelShareContext cancelShareContext) {
        List<Long> shareIdList = cancelShareContext.getShareIdList();
        if (!removeByIds(shareIdList)) {
            throw new XPanBusinessException("取消分享失败");
        }
    }

    /**
     * 校验用户是否拥有取消分享的权限
     *
     * @param cancelShareContext
     */
    private void checkUserCancelSharePermission(CancelShareContext cancelShareContext) {
        List<Long> shareIdList = cancelShareContext.getShareIdList();
        Long userId = cancelShareContext.getUserId();
        List<XPanShare> xPanShares = listByIds(shareIdList);
        if (CollectionUtils.isEmpty(xPanShares)) {
            throw new XPanBusinessException("您无权限操作取消分享的动作");
        }
        for (XPanShare record : xPanShares) {
            if (!Objects.equals(userId, record.getCreateUser())) {
                throw new XPanBusinessException("您无权限操作取消分享的动作");
            }
        }
    }

    /**
     * 拼装对应的返回VO
     *
     * @param shareUrlPO2CreateShareUrlContext
     * @return
     */
    private XPanShareUrlVO assembleShareVO(CreateShareUrlContext shareUrlPO2CreateShareUrlContext) {
        XPanShare record = shareUrlPO2CreateShareUrlContext.getRecord();
        XPanShareUrlVO vo = new XPanShareUrlVO();
        vo.setShareId(record.getShareId());
        vo.setShareName(record.getShareName());
        vo.setShareUrl(record.getShareUrl());
        vo.setShareCode(record.getShareCode());
        vo.setShareStatus(record.getShareStatus());
        return vo;
    }

    /**
     * 保存分享和分享文件的关联关系
     *
     * @param shareUrlPO2CreateShareUrlContext
     */
    private void saveShareFiles(CreateShareUrlContext shareUrlPO2CreateShareUrlContext) {
        SaveShareFilesContext saveShareFilesContext = new SaveShareFilesContext();
        saveShareFilesContext.setShareFileIdList(shareUrlPO2CreateShareUrlContext.getShareFileIdList());
        saveShareFilesContext.setShareId(shareUrlPO2CreateShareUrlContext.getRecord().getShareId());
        saveShareFilesContext.setUserId(shareUrlPO2CreateShareUrlContext.getUserId());
        xPanShareFileService.saveShareFiles(saveShareFilesContext);
    }

    /**
     * 拼装分享的实体，并保存到数据库中
     *
     * @param shareUrlPO2CreateShareUrlContext
     */
    private void saveShare(CreateShareUrlContext shareUrlPO2CreateShareUrlContext) {
        XPanShare xPanShare = new XPanShare();
        xPanShare.setShareId(IdUtil.get());
        xPanShare.setShareName(shareUrlPO2CreateShareUrlContext.getShareName());
        xPanShare.setShareType(shareUrlPO2CreateShareUrlContext.getShareType());
        xPanShare.setShareDayType(shareUrlPO2CreateShareUrlContext.getShareDayType());
        Integer shareDay = ShareDayTypeEnum.getShareDayByCode(shareUrlPO2CreateShareUrlContext.getShareDayType());
        if (Objects.equals(XPanConstants.MINUS_ONE_INT, shareDay)) {
            throw new XPanBusinessException("分享天数非法");
        }
        xPanShare.setShareDay(shareDay);
        xPanShare.setShareEndTime(DateUtil.offsetDay(new Date(), shareDay));
        xPanShare.setShareUrl(createShareUrl(xPanShare.getShareId()));
        xPanShare.setShareCode(createShareCode());
        xPanShare.setShareStatus(ShareStatusEnum.NORMAL.getCode());
        xPanShare.setCreateUser(shareUrlPO2CreateShareUrlContext.getUserId());
        xPanShare.setCreateTime(new Date());

        if (!save(xPanShare)) {
            throw new XPanBusinessException("保存分享信息失败");
        }

        shareUrlPO2CreateShareUrlContext.setRecord(xPanShare);
    }

    /**
     * 创建分享码
     *
     * @return
     */
    private String createShareCode() {
        return RandomStringUtils.randomAlphabetic(4).toLowerCase();
    }

    /**
     * 创建分享链接
     *
     * @param shareId
     * @return
     */
    private String createShareUrl(Long shareId) {
        if (Objects.isNull(shareId)) {
            throw new XPanBusinessException("分享的ID不能为空");
        }
        String sharePrefix = panServerConfig.getSharePrefix();
        if (sharePrefix.lastIndexOf(XPanConstants.SLASH_STR) == XPanConstants.MINUS_ONE_INT) {
            sharePrefix += XPanConstants.SLASH_STR;
        }
        return sharePrefix;
    }
}




