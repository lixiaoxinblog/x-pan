package com.xiaoxin.pan.server.modules.share.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.core.response.ResponseCode;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.core.utils.JwtUtil;
import com.xiaoxin.pan.core.utils.UUIDUtil;
import com.xiaoxin.pan.server.common.cache.AbstractManualCacheService;
import com.xiaoxin.pan.server.common.cache.ManualCacheService;
import com.xiaoxin.pan.server.common.config.PanServerConfig;
import com.xiaoxin.pan.server.modules.file.context.CopyFileContext;
import com.xiaoxin.pan.server.modules.file.context.FileDownloadContext;
import com.xiaoxin.pan.server.modules.file.context.QueryFileListContext;
import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.xiaoxin.pan.server.modules.file.enums.DelFlagEnum;
import com.xiaoxin.pan.server.modules.file.service.XPanUserFileService;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import com.xiaoxin.pan.server.modules.share.constants.ShareConstants;
import com.xiaoxin.pan.server.modules.share.context.*;
import com.xiaoxin.pan.server.modules.share.enmus.ShareDayTypeEnum;
import com.xiaoxin.pan.server.modules.share.enmus.ShareStatusEnum;
import com.xiaoxin.pan.server.modules.share.entity.XPanShare;
import com.xiaoxin.pan.server.modules.share.entity.XPanShareFile;
import com.xiaoxin.pan.server.modules.share.po.CheckShareCodePO;
import com.xiaoxin.pan.server.modules.share.service.XPanShareFileService;
import com.xiaoxin.pan.server.modules.share.service.XPanShareService;
import com.xiaoxin.pan.server.modules.share.mapper.XPanShareMapper;
import com.xiaoxin.pan.server.modules.share.vo.*;
import com.xiaoxin.pan.server.modules.user.entity.XPanUser;
import com.xiaoxin.pan.server.modules.user.service.XPanUserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private XPanUserFileService xPanUserFileService;
    @Autowired
    private XPanUserService xPanUserService;

    @Autowired
    @Qualifier("shareManualCacheService")
    private ManualCacheService<XPanShare> manualCacheService;
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
     * 校验分享码是否正确
     * 1、检查分享的状态是不是正常
     * 2、校验分享的分享码是不是正确
     * 3、生成一个短时间的分享token 返回给上游
     *
     * @param checkShareCodeContext
     * @return
     */
    @Override
    public String checkShareCode(CheckShareCodeContext checkShareCodeContext) {
        XPanShare xPanShare = checkShareStatus(checkShareCodeContext.getShareId());
        checkShareCodeContext.setRecord(xPanShare);
        doCheckShareCode(checkShareCodeContext);
        return generateShareToken(checkShareCodeContext);
    }

    /**
     * 查询分享的详情
     * 1、校验分享的状态
     * 2、初始化分享实体
     * 3、查询分享的主体信息
     * 4、查询分享的文件列表
     * 5、查询分享者的信息
     *
     * @param queryShareDetailContext
     * @return
     */
    @Override
    public ShareDetailVO detail(QueryShareDetailContext queryShareDetailContext) {
        XPanShare xPanShare = checkShareStatus(queryShareDetailContext.getShareId());
        queryShareDetailContext.setRecord(xPanShare);
        initShareVO(queryShareDetailContext);
        assembleMainShareInfo(queryShareDetailContext);
        assembleShareFilesInfo(queryShareDetailContext);
        assembleShareUserInfo(queryShareDetailContext);
        return queryShareDetailContext.getVo();
    }

    /**
     * 查询分享的简单详情
     * 1、校验分享的状态
     * 2、初始化分享实体
     * 3、查询分享的主体信息
     * 4、查询分享者的信息
     *
     * @param context
     * @return
     */
    @Override
    public ShareSimpleDetailVO simpleDetail(QueryShareSimpleDetailContext context) {
        XPanShare xPanShare = checkShareStatus(context.getShareId());
        context.setRecord(xPanShare);
        initShareSimpleVO(context);
        assembleMainShareSimpleInfo(context);
        assembleShareSimpleUserInfo(context);
        return context.getVo();
    }

    /**
     * 获取下一级的文件列表
     * 1、校验分享的状态
     * 2、校验文件的ID实在分享的文件列表中
     * 3、查询对应文件的子文件列表，返回
     *
     * @param queryChildFileListContext
     * @return
     */
    @Override
    public List<XPanUserFileVO> fileList(QueryChildFileListContext queryChildFileListContext) {
        XPanShare record = checkShareStatus(queryChildFileListContext.getShareId());
        queryChildFileListContext.setRecord(record);
        List<XPanUserFileVO> allUserFileRecords = checkFileIdIsOnShareStatusAndGetAllShareUserFiles(
                queryChildFileListContext.getShareId(), Lists.newArrayList(queryChildFileListContext.getParentId())
        );
        Map<Long, List<XPanUserFileVO>> parentIdFileListMap = allUserFileRecords
                .stream()
                .collect(Collectors.groupingBy(XPanUserFileVO::getParentId));
        List<XPanUserFileVO> xPanUserFileVOS = parentIdFileListMap.get(queryChildFileListContext.getParentId());
        if (CollectionUtils.isEmpty(xPanUserFileVOS)) {
            return Lists.newArrayList();
        }
        return xPanUserFileVOS;
    }

    /**
     * 保存分享的文件
     * 1、校验分享状态
     * 2、校验文件ID是否合法
     * 3、执行保存我的网盘动作
     *
     * @param context
     */
    @Override
    public void saveFiles(ShareSaveContext context) {
        checkShareStatus(context.getShareId());
        checkFileIdIsOnShareStatus(context.getShareId(), context.getFileIdList());
        doSave(context);
    }

    /**
     * 分享的文件下载
     * 1、校验分享状态
     * 2、校验文件ID的合法性
     * 3、执行文件下载的动作
     * @param shareFileDownloadContext
     */
    @Override
    public void download(ShareFileDownloadContext shareFileDownloadContext) {
        checkShareStatus(shareFileDownloadContext.getShareId());
        checkFileIdIsOnShareStatus(shareFileDownloadContext.getShareId(),Lists.newArrayList(shareFileDownloadContext.getFileId()));
        doDownload(shareFileDownloadContext);
    }

    /**
     * 执行文件下载操作
     * @param shareFileDownloadContext
     */
    private void doDownload(ShareFileDownloadContext shareFileDownloadContext) {
        FileDownloadContext fileDownloadContext = new FileDownloadContext();
        fileDownloadContext.setFileId(shareFileDownloadContext.getFileId());
        fileDownloadContext.setUserId(shareFileDownloadContext.getUserId());
        fileDownloadContext.setResponse(shareFileDownloadContext.getResponse());
        xPanUserFileService.download(fileDownloadContext);
    }

    /**
     * 执行保存动作
     * 委托文件模块做文件拷贝的操作
     * @param context
     */
    private void doSave(ShareSaveContext context) {
        CopyFileContext copyFileContext = new CopyFileContext();
        copyFileContext.setFileIdList(context.getFileIdList());
        copyFileContext.setTargetParentId(context.getTargetParentId());
        copyFileContext.setUserId(context.getUserId());
        xPanUserFileService.copy(copyFileContext);
    }

    /**
     * 校验文件ID是否属于某一个分享
     *
     * @param shareId
     * @param fileIdList
     */
    private void checkFileIdIsOnShareStatus(Long shareId, List<Long> fileIdList) {
        checkFileIdIsOnShareStatusAndGetAllShareUserFiles(shareId, fileIdList);
    }

    /**
     * 校验文件是否处于分享状态，返回该分享的所有文件列表
     *
     * @param shareId
     * @param fileIdList
     * @return
     */
    private List<XPanUserFileVO> checkFileIdIsOnShareStatusAndGetAllShareUserFiles(Long shareId, List<Long> fileIdList) {
        List<Long> shareFileIdList = getShareFileIdList(shareId);
        if (CollectionUtils.isEmpty(shareFileIdList)) {
            return Lists.newArrayList();
        }
        List<XPanUserFile> allFileRecords = xPanUserFileService.findAllFileRecordsByFiledIdlist(shareFileIdList);
        if (CollectionUtils.isEmpty(allFileRecords)) {
            return Lists.newArrayList();
        }
        allFileRecords = allFileRecords.stream()
                .filter(Objects::nonNull)
                .filter(record -> Objects.equals(record.getDelFlag(), DelFlagEnum.NO.getCode()))
                .collect(Collectors.toList());
        List<Long> allFileIdList = allFileRecords.stream().map(XPanUserFile::getFileId).collect(Collectors.toList());
        if (allFileIdList.containsAll(fileIdList)) {
            return xPanUserFileService.transferVOList(allFileRecords);
        }
        throw new XPanBusinessException(ResponseCode.SHARE_FILE_MISS);
    }

    /**
     * 拼装简单文件分享详情的用户信息
     *
     * @param context
     */
    private void assembleShareSimpleUserInfo(QueryShareSimpleDetailContext context) {
        XPanUser xPanUser = xPanUserService.getById(context.getRecord().getCreateUser());
        if (Objects.isNull(xPanUser)) {
            throw new XPanBusinessException("用户信息查询失败");
        }
        ShareUserInfoVO shareUserInfoVO = new ShareUserInfoVO();
        shareUserInfoVO.setUserId(xPanUser.getUserId());
        shareUserInfoVO.setUsername(encryptUsername(xPanUser.getUsername()));
        context.getVo().setShareUserInfoVO(shareUserInfoVO);
    }

    /**
     * 加密用户名称
     *
     * @param username
     * @return
     */
    private String encryptUsername(String username) {
        StringBuffer stringBuffer = new StringBuffer(username);
        stringBuffer.replace(XPanConstants.TWO_INT, username.length() - XPanConstants.TWO_INT, XPanConstants.COMMON_ENCRYPT_STR);
        return stringBuffer.toString();
    }

    /**
     * 填充简单分享详情实体信息
     *
     * @param context
     */
    private void assembleMainShareSimpleInfo(QueryShareSimpleDetailContext context) {
        XPanShare record = context.getRecord();
        ShareSimpleDetailVO vo = context.getVo();
        vo.setShareId(record.getShareId());
        vo.setShareName(record.getShareName());
    }

    /**
     * 初始化简单分享详情的VO对象
     *
     * @param context
     */
    private void initShareSimpleVO(QueryShareSimpleDetailContext context) {
        ShareSimpleDetailVO shareSimpleDetailVO = new ShareSimpleDetailVO();
        context.setVo(shareSimpleDetailVO);
    }

    /**
     * @param queryShareDetailContext
     */
    private void assembleShareUserInfo(QueryShareDetailContext queryShareDetailContext) {
        XPanUser xPanUser = xPanUserService.getById(queryShareDetailContext.getRecord().getCreateUser());
        if (Objects.isNull(xPanUser)) {
            throw new XPanBusinessException("用户信息查询失败");
        }
        ShareUserInfoVO shareUserInfoVO = new ShareUserInfoVO();
        shareUserInfoVO.setUserId(xPanUser.getUserId());
        shareUserInfoVO.setUsername(xPanUser.getUsername());
        queryShareDetailContext.getVo().setShareUserInfoVO(shareUserInfoVO);
    }

    /**
     * 查询分享对应的文件列表
     * 1、查询分享对应的文件ID集合
     * 2、根据文件ID来查询文件列表信息
     *
     * @param queryShareDetailContext
     */
    private void assembleShareFilesInfo(QueryShareDetailContext queryShareDetailContext) {
        List<Long> fileIdList = getShareFileIdList(queryShareDetailContext.getShareId());
        QueryFileListContext queryFileListContext = new QueryFileListContext();
        queryFileListContext.setUserId(queryShareDetailContext.getRecord().getCreateUser());
        queryFileListContext.setDelFlag(DelFlagEnum.NO.getCode());
        queryFileListContext.setFileIdList(fileIdList);
        List<XPanUserFileVO> xPanUserFileVOList = xPanUserFileService.getFileList(queryFileListContext);
        queryShareDetailContext.getVo().setXPanUserFileVOList(xPanUserFileVOList);
    }

    /**
     * 查询分享对应的文件ID集合
     *
     * @param shareId
     * @return
     */
    private List<Long> getShareFileIdList(Long shareId) {
        if (Objects.isNull(shareId)) {
            return Lists.newArrayList();
        }
        LambdaQueryWrapper<XPanShareFile> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(XPanShareFile::getFileId);
        queryWrapper.eq(XPanShareFile::getShareId, shareId);
        return xPanShareFileService.listObjs(queryWrapper, v -> (Long) v);
    }

    /**
     * 查询分享的主体信息
     *
     * @param queryShareDetailContext
     */
    private void assembleMainShareInfo(QueryShareDetailContext queryShareDetailContext) {
        XPanShare record = queryShareDetailContext.getRecord();
        ShareDetailVO vo = queryShareDetailContext.getVo();
        vo.setShareId(record.getShareId());
        vo.setShareName(record.getShareName());
        vo.setCreateTime(record.getCreateTime());
        vo.setShareDay(record.getShareDay());
        vo.setShareEndTime(record.getShareEndTime());
    }

    /**
     * 初始化分享实体
     *
     * @param queryShareDetailContext
     */
    private void initShareVO(QueryShareDetailContext queryShareDetailContext) {
        ShareDetailVO shareDetailVO = new ShareDetailVO();
        queryShareDetailContext.setVo(shareDetailVO);
    }

    /**
     * 生成一个短期的token
     *
     * @param checkShareCodeContext
     * @return
     */
    private String generateShareToken(CheckShareCodeContext checkShareCodeContext) {
        XPanShare record = checkShareCodeContext.getRecord();
        return JwtUtil.generateToken(UUIDUtil.getUUID(), ShareConstants.SHARE_ID, record.getShareId(), ShareConstants.ONE_HOUR_LONG);
    }

    /**
     * 校验分享码
     * 校验分享码是不是正确
     *
     * @param checkShareCodeContext
     */
    private void doCheckShareCode(CheckShareCodeContext checkShareCodeContext) {
        if (!Objects.equals(checkShareCodeContext.getRecord().getShareCode(), checkShareCodeContext.getShareCode())) {
            throw new XPanBusinessException("分享码错误！");
        }
    }

    /**
     * 校验分享状态是否正常
     *
     * @param shareId
     * @return
     */
    private XPanShare checkShareStatus(Long shareId) {
        XPanShare record = getById(shareId);
        if (Objects.isNull(record)) {
            throw new XPanBusinessException(ResponseCode.SHARE_CANCELLED);
        }
        if (Objects.equals(ShareStatusEnum.FILE_DELETED.getCode(), record.getShareStatus())) {
            throw new XPanBusinessException(ResponseCode.SHARE_FILE_MISS);
        }
        if (Objects.equals(ShareDayTypeEnum.PERMANENT_VALIDITY.getCode(), record.getShareDayType())) {
            return record;
        }
        if (record.getShareEndTime().before(new Date())) {
            throw new XPanBusinessException(ResponseCode.SHARE_EXPIRE);
        }
        return record;
    }

    /**
     * 取消文件和分享的关联关系数据
     *
     * @param cancelShareContext
     */
    private void doCancelShareFiles(CancelShareContext cancelShareContext) {
        LambdaQueryWrapper<XPanShareFile> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(XPanShareFile::getShareId, cancelShareContext.getShareIdList());
        queryWrapper.eq(XPanShareFile::getCreateUser, cancelShareContext.getUserId());
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
        return sharePrefix + IdUtil.encrypt(shareId);
    }

    @Override
    public boolean updateById(XPanShare entity) {
//        return super.updateById(entity);
        return manualCacheService.updateById(entity.getShareId(), entity);
    }

    @Override
    public boolean updateBatchById(Collection<XPanShare> entityList) {
//        return super.updateBatchById(entityList);
        Map<Long, XPanShare> collect = entityList.stream().collect(Collectors.toMap(XPanShare::getShareId, v -> v));
        return manualCacheService.updateByIds(collect);
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
//        return super.removeByIds(idList);
        return manualCacheService.removeByIds(idList);
    }

    @Override
    public boolean removeById(Serializable id) {
//        return super.removeById(id);
        return manualCacheService.removeById(id);
    }

    @Override
    public XPanShare getById(Serializable id) {
//        return super.getById(id);
        return manualCacheService.getById(id);
    }

    @Override
    public List<XPanShare> listByIds(Collection<? extends Serializable> idList) {
//        return super.listByIds(idList);
        return manualCacheService.getByIds(idList);
    }
}




