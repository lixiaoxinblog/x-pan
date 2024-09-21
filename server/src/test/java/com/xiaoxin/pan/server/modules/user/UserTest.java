package com.xiaoxin.pan.server.modules.user;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.core.utils.JwtUtil;
import com.xiaoxin.pan.server.modules.user.constants.UserConstants;
import com.xiaoxin.pan.server.modules.user.context.CheckUsernameContext;
import com.xiaoxin.pan.server.modules.user.context.UserLoginContext;
import com.xiaoxin.pan.server.modules.user.context.UserRegisterContext;
import com.xiaoxin.pan.server.modules.user.service.XPanUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

//@Transactional
@SpringBootTest(classes = com.xiaoxin.pan.server.XPanServerLauncher.class)
public class UserTest {

    @Autowired
    private XPanUserService xPanUserService;

    //测试注册用户
    @Test
    public void testRegisterUser() {
        UserRegisterContext userRegisterContext = createUserRegisterContext();
        Long register = xPanUserService.register(userRegisterContext);
        Assert.isTrue(register.longValue() > 0L);
    }

    /**
     * 构建注册用户上下文信息
     *
     * @return
     */
    private UserRegisterContext createUserRegisterContext() {
        UserRegisterContext context = new UserRegisterContext();
        context.setUsername("xiaoxin");
        context.setPassword("123456789");
        context.setQuestion("question");
        context.setAnswer("answer");
        return context;
    }

    /**
     * 测试用户幂等注册
     */
    @Test
    public void testRegisterDuplicateUsername() {
        UserRegisterContext userRegisterContext = createUserRegisterContext();
        Long register = xPanUserService.register(userRegisterContext);
        Assert.isTrue(register.longValue() > 0L);
        xPanUserService.register(userRegisterContext);
    }

    /**
     * 测试用户登录成功
     */
    @Test
    public void testLogin() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = xPanUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        UserLoginContext userLoginContext = createUserLoginContext();
        String accessToken = xPanUserService.login(userLoginContext);

        Assert.isTrue(StringUtils.isNotBlank(accessToken));
    }

    /**
     * 测试用户登录失败：用户名不正确
     */
    @Test
    public void testLoginUsernameError() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = xPanUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        UserLoginContext userLoginContext = createUserLoginContext();
        userLoginContext.setUsername(userLoginContext.getUsername() + "_change");
        Assertions.assertThrows(XPanBusinessException.class,()->{
            xPanUserService.login(userLoginContext);
        });
    }

    /**
     * 测试用户登录失败：密码不正确
     */
    @Test()
    public void testLoginPasswordError() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = xPanUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        UserLoginContext userLoginContext = createUserLoginContext();
        userLoginContext.setPassword(userLoginContext.getPassword() + "_change");
        xPanUserService.login(userLoginContext);
    }

    /**
     * 构建用户登录上下文实体
     *
     * @return
     */
    private UserLoginContext createUserLoginContext() {
        UserLoginContext userLoginContext = new UserLoginContext();
        userLoginContext.setUsername("xiaoxin");
        userLoginContext.setPassword("123456789");
        return userLoginContext;
    }

    /**
     * 用户成功登出
     */
    @Test
    public void exitSuccess() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = xPanUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        UserLoginContext userLoginContext = createUserLoginContext();
        String accessToken = xPanUserService.login(userLoginContext);

        Assert.isTrue(StringUtils.isNotBlank(accessToken));

        Long userId = (Long) JwtUtil.analyzeToken(accessToken, UserConstants.LOGIN_USER_ID);

        xPanUserService.exit(userId);
    }

    /**
     * 检查用户名是否存在
     */
    @Test
    public void checkUsername() {
        CheckUsernameContext checkUsernameContext = new CheckUsernameContext();
        checkUsernameContext.setUsername("xiaoxin");
        xPanUserService.checkUsername(checkUsernameContext);
    }

}
