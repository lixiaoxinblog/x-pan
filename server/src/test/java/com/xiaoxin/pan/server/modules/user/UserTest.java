package com.xiaoxin.pan.server.modules.user;

import cn.hutool.core.lang.Assert;
import com.xiaoxin.pan.server.modules.user.context.UserRegisterContext;
import com.xiaoxin.pan.server.modules.user.service.XPanUserService;
import org.junit.jupiter.api.Test;
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

}
