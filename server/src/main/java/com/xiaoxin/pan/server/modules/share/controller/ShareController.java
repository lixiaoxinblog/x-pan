package com.xiaoxin.pan.server.modules.share.controller;

import com.google.common.base.Splitter;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.response.R;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.common.utils.UserIdUtil;
import com.xiaoxin.pan.server.modules.share.context.CancelShareContext;
import com.xiaoxin.pan.server.modules.share.context.CreateShareUrlContext;
import com.xiaoxin.pan.server.modules.share.context.QueryShareListContext;
import com.xiaoxin.pan.server.modules.share.converter.ShareConverter;
import com.xiaoxin.pan.server.modules.share.po.CancelSharePO;
import com.xiaoxin.pan.server.modules.share.po.CreateShareUrlPO;
import com.xiaoxin.pan.server.modules.share.service.XPanShareService;
import com.xiaoxin.pan.server.modules.share.vo.XPanShareUrlListVO;
import com.xiaoxin.pan.server.modules.share.vo.XPanShareUrlVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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


}
