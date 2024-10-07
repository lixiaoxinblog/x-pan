package com.xiaoxin.pan.server.modules.share.controller;

import com.google.common.base.Splitter;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.response.R;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.common.annotation.LoginIgnore;
import com.xiaoxin.pan.server.common.annotation.NeedShareCode;
import com.xiaoxin.pan.server.common.utils.ShareIdUtil;
import com.xiaoxin.pan.server.common.utils.UserIdUtil;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import com.xiaoxin.pan.server.modules.share.context.*;
import com.xiaoxin.pan.server.modules.share.converter.ShareConverter;
import com.xiaoxin.pan.server.modules.share.po.CancelSharePO;
import com.xiaoxin.pan.server.modules.share.po.CheckShareCodePO;
import com.xiaoxin.pan.server.modules.share.po.CreateShareUrlPO;
import com.xiaoxin.pan.server.modules.share.po.ShareSavePO;
import com.xiaoxin.pan.server.modules.share.service.XPanShareService;
import com.xiaoxin.pan.server.modules.share.vo.ShareDetailVO;
import com.xiaoxin.pan.server.modules.share.vo.ShareSimpleDetailVO;
import com.xiaoxin.pan.server.modules.share.vo.XPanShareUrlListVO;
import com.xiaoxin.pan.server.modules.share.vo.XPanShareUrlVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件分享控制器
 */
@RestController
@RequestMapping("/share")
public class ShareController {

    @Autowired
    private XPanShareService xPanShareService;
    @Autowired
    private ShareConverter shareConverter;

    /**
     *创建分享连接
     * @return
     */

    @ApiOperation(
            value = "创建分享链接",
            notes = "该接口提供了创建分享链接的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping
    public R<XPanShareUrlVO> create(@Validated @RequestBody CreateShareUrlPO createShareUrlPO){
        CreateShareUrlContext shareUrlPO2CreateShareUrlContext = shareConverter.createShareUrlPO2CreateShareUrlContext(createShareUrlPO);
        String shareFileIds = createShareUrlPO.getShareFileIds();
        List<Long> shareFileIdList = Splitter.on(XPanConstants.COMMON_SEPARATOR)
                .splitToList(shareFileIds)
                .stream()
                .map(IdUtil::decrypt)
                .collect(Collectors.toList());
        shareUrlPO2CreateShareUrlContext.setShareFileIdList(shareFileIdList);
        XPanShareUrlVO  xPanShareUrlVO= xPanShareService.create(shareUrlPO2CreateShareUrlContext);
        return R.data(xPanShareUrlVO);
    }

    @ApiOperation(
            value = "查询分享链接列表",
            notes = "该接口提供了查询分享链接列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("shares")
    public R<List<XPanShareUrlListVO>> list(){
        QueryShareListContext queryShareListContext = new QueryShareListContext();
        queryShareListContext.setUserId(UserIdUtil.get());
        return R.data(xPanShareService.getShares(queryShareListContext));
    }
    @ApiOperation(
            value = "取消分享",
            notes = "该接口提供了取消分享的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @DeleteMapping()
    public R cancelShare(@Validated @RequestBody CancelSharePO cancelSharePO) {
        CancelShareContext cancelShareContext = new CancelShareContext();

        cancelShareContext.setUserId(UserIdUtil.get());

        String shareIds = cancelSharePO.getShareIds();
        List<Long> shareIdList = Splitter.on(XPanConstants.COMMON_SEPARATOR)
                .splitToList(shareIds)
                .stream()
                .map(IdUtil::decrypt)
                .collect(Collectors.toList());
        cancelShareContext.setShareIdList(shareIdList);
        xPanShareService.cancelShare(cancelShareContext);
        return R.success();
    }

    @ApiOperation(
            value = "校验分享码",
            notes = "该接口提供了校验分享码的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @PostMapping("/code/check")
    public R<String> checkShareCode(@Validated @RequestBody CheckShareCodePO checkShareCodePO){
        CheckShareCodeContext checkShareCodeContext = new CheckShareCodeContext();
        checkShareCodeContext.setShareCode(checkShareCodePO.getShareCode());
        checkShareCodeContext.setShareId(IdUtil.decrypt(checkShareCodePO.getShareId()));
        String token =  xPanShareService.checkShareCode(checkShareCodeContext);
        return R.data(token);
    }

    @ApiOperation(
            value = "查询分享的详情",
            notes = "该接口提供了查询分享的详情的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @NeedShareCode
    @GetMapping()
    public R<ShareDetailVO> detail() {
        QueryShareDetailContext queryShareDetailContext = new QueryShareDetailContext();
        queryShareDetailContext.setShareId(ShareIdUtil.get());
        ShareDetailVO vo = xPanShareService.detail(queryShareDetailContext);
        return R.data(vo);
    }

    @ApiOperation(
            value = "查询分享的简单详情",
            notes = "该接口提供了查询分享的简单详情的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @GetMapping("/simple")
    public R<ShareSimpleDetailVO> simpleDetail(@NotBlank(message = "分享的ID不能为空") @RequestParam(value = "shareId", required = false) String shareId) {
        QueryShareSimpleDetailContext context = new QueryShareSimpleDetailContext();
        context.setShareId(IdUtil.decrypt(shareId));
        ShareSimpleDetailVO vo = xPanShareService.simpleDetail(context);
        return R.data(vo);
    }

    @ApiOperation(
            value = "获取下一级文件列表",
            notes = "该接口提供了获取下一级文件列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/file/list")
    @NeedShareCode
    @LoginIgnore
    public R<List<XPanUserFileVO>> fileList(@NotBlank(message = "文件的父ID不能为空") @RequestParam(value = "parentId", required = false) String parentId) {
        QueryChildFileListContext queryChildFileListContext = new QueryChildFileListContext();
        queryChildFileListContext.setShareId(ShareIdUtil.get());
        queryChildFileListContext.setParentId(IdUtil.decrypt(parentId));
        List<XPanUserFileVO> result = xPanShareService.fileList(queryChildFileListContext);
        return R.data(result);
    }

    /**
     * 保存至我的网盘
     */
    @ApiOperation(
            value = "保存至我的网盘",
            notes = "该接口提供了保存至我的网盘的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @NeedShareCode
    @PostMapping("/save")
    public R saveFiles(@Validated @RequestBody ShareSavePO shareSavePO) {
        ShareSaveContext context = new ShareSaveContext();

        String fileIds = shareSavePO.getFileIds();
        List<Long> fileIdList = Splitter
                .on(XPanConstants.COMMON_SEPARATOR)
                .splitToList(fileIds)
                .stream()
                .map(IdUtil::decrypt)
                .collect(Collectors.toList());
        context.setFileIdList(fileIdList);

        context.setTargetParentId(IdUtil.decrypt(shareSavePO.getTargetParentId()));
        context.setUserId(UserIdUtil.get());
        context.setShareId(ShareIdUtil.get());

        xPanShareService.saveFiles(context);
        return R.success();
    }




}
