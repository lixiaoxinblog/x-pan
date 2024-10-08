package com.xiaoxin.pan.server.modules.user.converter;

import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.xiaoxin.pan.server.modules.user.context.*;
import com.xiaoxin.pan.server.modules.user.entity.XPanUser;
import com.xiaoxin.pan.server.modules.user.po.*;
import com.xiaoxin.pan.server.modules.user.vo.XPanUserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 用户模块实体转化工具类
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    /**
     * UserRegisterPO转化成UserRegisterContext
     *
     * @param userRegisterPO
     * @return
     */
    UserRegisterContext userRegisterPO2UserRegisterContext(UserRegisterPO userRegisterPO);

    /**
     * UserRegisterContext转XPanUser
     *
     * @param userRegisterContext
     * @return
     */
    @Mapping(target = "password", ignore = true)
    XPanUser userRegisterContext2RPanUser(UserRegisterContext userRegisterContext);

    /**
     * UserLoginPO转UserLoginContext
     *
     * @param userLoginPO
     * @return
     */
    UserLoginContext userLoginPO2UserLoginContext(UserLoginPO userLoginPO);

    /**
     * CheckUsernamePO转CheckUsernameContext
     *
     * @param checkUsernamePO
     * @return
     */
    CheckUsernameContext checkUsernamePO2CheckUsernameContext(CheckUsernamePO checkUsernamePO);


    /**
     * CheckAnswerPO转CheckAnswerContext
     *
     * @param checkAnswerPO
     * @return
     */
    CheckAnswerContext checkAnswerPO2CheckAnswerContext(CheckAnswerPO checkAnswerPO);

    /**
     * ResetPasswordPO转ResetPasswordContext
     *
     * @param resetPasswordPO
     * @return
     */
    ResetPasswordContext resetPasswordPO2ResetPasswordContext(ResetPasswordPO resetPasswordPO);

    /**
     * ChangePasswordPO转ChangePasswordContext
     *
     * @param changePasswordPO
     * @return
     */
    ChangePasswordContext changePasswordPO2ChangePasswordContext(ChangePasswordPO changePasswordPO);

    /**
     * 拼装用户基本信息返回实体
     */
    @Mapping(source = "xPanUser.username", target = "username")
    @Mapping(source = "xPanUserFile.fileId", target = "rootFileId")
    @Mapping(source = "xPanUserFile.filename", target = "rootFilename")
    XPanUserVO assembleUserInfoVO(XPanUser xPanUser, XPanUserFile xPanUserFile);
}
