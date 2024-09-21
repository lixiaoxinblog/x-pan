package com.xiaoxin.pan.server.modules.user.service;

import com.xiaoxin.pan.server.modules.user.context.*;
import com.xiaoxin.pan.server.modules.user.entity.XPanUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoxin.pan.server.modules.user.vo.XPanUserVO;

/**
* @author xiaoxin
* @description 针对表【x_pan_user(用户信息表)】的数据库操作Service
* @createDate 2024-07-26 12:51:06
*/
public interface XPanUserService extends IService<XPanUser> {

    Long register(UserRegisterContext userRegisterContext);

    String login(UserLoginContext userLoginContext);

    void exit(Long aLong);

    String checkUsername(CheckUsernameContext checkUsernameContext);

    String checkAnswer(CheckAnswerContext checkAnswerContext);

    void resetPassword(ResetPasswordContext resetPasswordContext);

    void changePassword(ChangePasswordContext changePasswordContext);

    XPanUserVO info(Long userId);
}
