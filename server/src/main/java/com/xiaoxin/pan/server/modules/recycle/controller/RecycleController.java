package com.xiaoxin.pan.server.modules.recycle.controller;

import com.google.common.base.Splitter;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.response.R;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.common.utils.UserIdUtil;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import com.xiaoxin.pan.server.modules.recycle.context.QueryRecycleFileListContext;
import com.xiaoxin.pan.server.modules.recycle.context.RestoreContext;
import com.xiaoxin.pan.server.modules.recycle.po.RestorePO;
import com.xiaoxin.pan.server.modules.recycle.service.XPanUserRecycleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recycle")
public class RecycleController {

    @Autowired
    private XPanUserRecycleService xPanUserRecycleService;


    /**
     * 获取用户回收站文件列表
     * @return
     */
    @ApiOperation(
            value = "获取回收站文件列表",
            notes = "该接口提供了获取回收站文件列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping()
    public R<List<XPanUserFileVO>> list(){
        QueryRecycleFileListContext queryRecycleFileListContext = new QueryRecycleFileListContext();
        queryRecycleFileListContext.setUserId(UserIdUtil.get());
        List<XPanUserFileVO> xPanUserFileVOList = xPanUserRecycleService.list(queryRecycleFileListContext);
        return R.data(xPanUserFileVOList);
    }

    /**
     * 还原回收站文件
     */
    @ApiOperation(
            value = "还原回收站文件",
            notes = "该接口提供了还原回收站文件功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PutMapping("/restore")
    public R restore(@Validated @RequestBody RestorePO restorePO){
        RestoreContext restoreContext = new RestoreContext();
        List<Long> ids = Splitter.on(XPanConstants.COMMON_SEPARATOR)
                .splitToList(restorePO.getFileIds())
                .stream().map(IdUtil::decrypt)
                .collect(Collectors.toList());
        restoreContext.setFileIdList(ids);
        restoreContext.setUserId(UserIdUtil.get());
        xPanUserRecycleService.restore(restoreContext);
        return R.success();
    }

}
