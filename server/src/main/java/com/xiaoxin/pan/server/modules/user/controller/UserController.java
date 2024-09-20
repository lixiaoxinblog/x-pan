package com.xiaoxin.pan.server.modules.user.controller;

import com.xiaoxin.pan.core.response.R;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.common.annotation.LoginIgnore;
import com.xiaoxin.pan.server.modules.user.context.UserLoginContext;
import com.xiaoxin.pan.server.modules.user.context.UserRegisterContext;
import com.xiaoxin.pan.server.modules.user.converter.UserConverter;
import com.xiaoxin.pan.server.modules.user.po.UserLoginPO;
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
    @LoginIgnore
    public R register(@Validated @RequestBody UserRegisterPO userRegisterPO) {
        UserRegisterContext userRegisterContext = userConverter.userRegisterPO2UserRegisterContext(userRegisterPO);
        Long userId = userService.register(userRegisterContext);
        return R.success(IdUtil.encrypt(userId));
    }

    /**
     * 用户登陆接口，登陆成功后返回有效时间的access token
     */
    @ApiOperation(
            value = "用户登陆接口",
            notes = "该接口提供了用户登陆的功能，登陆成功后返回有效时间的access token",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("login")
    @LoginIgnore
    public R login(@RequestBody UserLoginPO userLoginPO) {
        UserLoginContext userLoginContext = userConverter.userLoginPO2UserLoginContext(userLoginPO);
        String accessToken = userService.login(userLoginContext);
        return R.data(accessToken);
    }

    /**
     * 用户登出
     */
    @ApiOperation(
            value = "用户登出接口",
            notes = "该接口提供了用户登出的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("exit")
    public R exit() {
        userService.exit(IdUtil.get());
        return R.success("退出登录成功！");
    }

}
