package com.xiaoxin.pan.server.modules.user.controller;

import com.xiaoxin.pan.core.response.R;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.modules.user.context.UserRegisterContext;
import com.xiaoxin.pan.server.modules.user.converter.UserConverter;
import com.xiaoxin.pan.server.modules.user.po.UserRegisterPO;
import com.xiaoxin.pan.server.modules.user.service.XPanUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
@Api(tags = "用户模块")
public class UserController {
    @Autowired
    private XPanUserService userService;

    @Autowired
    private UserConverter userConverter;

    /*
    用户注册接口，实现幂等性。
     */
    @ApiOperation(
            value = "用户注册接口",
            notes = "该接口提供了用户注册的功能，实现了冥等性注册的逻辑，可以放心多并发调用",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("register")
    public R register(@Validated @RequestBody UserRegisterPO userRegisterPO) {
        UserRegisterContext userRegisterContext = userConverter.userRegisterPO2UserRegisterContext(userRegisterPO);
        Long userId = userService.register(userRegisterContext);
        return R.success(IdUtil.encrypt(userId));
    }

}
