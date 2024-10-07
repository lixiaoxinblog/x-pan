package com.xiaoxin.pan.server.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxin.pan.cache.core.constanst.CacheConstants;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.core.response.ResponseCode;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.core.utils.JwtUtil;
import com.xiaoxin.pan.core.utils.PasswordUtil;
import com.xiaoxin.pan.server.common.cache.AnnotationCacheService;
import com.xiaoxin.pan.server.modules.file.constants.FileConstants;
import com.xiaoxin.pan.server.modules.file.context.CreateFolderContext;
import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.xiaoxin.pan.server.modules.file.service.XPanUserFileService;
import com.xiaoxin.pan.server.modules.user.constants.UserConstants;
import com.xiaoxin.pan.server.modules.user.context.*;
import com.xiaoxin.pan.server.modules.user.converter.UserConverter;
import com.xiaoxin.pan.server.modules.user.entity.XPanUser;
import com.xiaoxin.pan.server.modules.user.service.XPanUserService;
import com.xiaoxin.pan.server.modules.user.mapper.XPanUserMapper;
import com.xiaoxin.pan.server.modules.user.vo.XPanUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author xiaoxin
 * @description 针对表【x_pan_user(用户信息表)】的数据库操作Service实现
 * @createDate 2024-07-26 12:51:06
 */
@Service
public class XPanUserServiceImpl extends ServiceImpl<XPanUserMapper, XPanUser>
        implements XPanUserService {


    @Autowired
    private UserConverter userConverter;

    @Autowired
    private XPanUserFileService xPanUserFileService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    @Qualifier(value = "userAnnotationCacheService")
    private AnnotationCacheService<XPanUser> userCacheService;

    public XPanUserServiceImpl(UserConverter userConverter) {
        this.userConverter = userConverter;
    }

    @Override
    public Long register(UserRegisterContext userRegisterContext) {
        //转换对象
        assembleUserEntity(userRegisterContext);
        //注册
        doRegister(userRegisterContext);
        //创建文件夹根目录
        createUserRootFolder(userRegisterContext);
        return userRegisterContext.getEntity().getUserId();
    }

    /**
     * 用户登录业务实现
     * 需要实现的功能：
     * 1、用户的登录信息校验
     * 2、生成一个具有时效性的accessToken
     * 3、将accessToken缓存起来，去实现单机登录
     */
    @Override
    public String login(UserLoginContext userLoginContext) {
        checkLoginInfo(userLoginContext);
        generateAndSaveAccessToken(userLoginContext);
        return userLoginContext.getAccessToken();
    }

    /**
     * 用户退出登录
     * 清除用户的登录凭证缓存
     */
    @Override
    public void exit(Long userId) {
        try {
            Cache cache = cacheManager.getCache(CacheConstants.X_PAN_CACHE_NAME);
            cache.evict(UserConstants.USER_LOGIN_PREFIX + userId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new XPanBusinessException("用户退出登录失败");
        }
    }

    /**
     * 用户忘记密码-校验用户名称
     */
    @Override
    public String checkUsername(CheckUsernameContext checkUsernameContext) {
        String question = baseMapper.selectQuestionByUsername(checkUsernameContext.getUsername());
        if (StringUtils.isBlank(question)) {
            throw new XPanBusinessException("没有此用户");
        }
        return question;
    }

    /**
     * 用户忘记密码-校验密保答案
     *
     * @return 临时token
     */
    @Override
    public String checkAnswer(CheckAnswerContext checkAnswerContext) {
        QueryWrapper<XPanUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", checkAnswerContext.getUsername());
        queryWrapper.eq("question", checkAnswerContext.getQuestion());
        queryWrapper.eq("answer", checkAnswerContext.getAnswer());
        int count = count(queryWrapper);

        if (count == 0) {
            throw new XPanBusinessException("密保答案错误");
        }

        return generateCheckAnswerToken(checkAnswerContext);
    }

    private String generateCheckAnswerToken(CheckAnswerContext checkAnswerContext) {
        return JwtUtil.generateToken(checkAnswerContext.getUsername(), UserConstants.FORGET_USERNAME, checkAnswerContext.getUsername(), UserConstants.FIVE_MINUTES_LONG);
    }

    /**
     * 重置用户密码
     * 1、校验token是不是有效
     * 2、重置密码
     */
    @Override
    public void resetPassword(ResetPasswordContext resetPasswordContext) {
        checkForgetPasswordToken(resetPasswordContext);
        checkAndResetUserPassword(resetPasswordContext);
    }

    /**
     * 在线修改密码
     * 1、校验旧密码
     * 2、重置新密码
     * 3、退出当前的登录状态
     */
    @Override
    public void changePassword(ChangePasswordContext changePasswordContext) {
        checkOldPassword(changePasswordContext);
        doChangePassword(changePasswordContext);
        exitLoginStatus(changePasswordContext);
    }

    /**
     * 查询在线用户基本信息及根目录信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public XPanUserVO info(Long userId) {
        XPanUser entity = getById(userId);
        if (Objects.isNull(entity)) {
            throw new XPanBusinessException("用户信息查询失败");
        }
        XPanUserFile xPanUserFile = getUserRootFileInfo(userId);
        if (Objects.isNull(xPanUserFile)) {
            throw new XPanBusinessException("用户根目录信息查询失败!");
        }
        return userConverter.assembleUserInfoVO(entity, xPanUserFile);
    }

    /**
     * 获取用户根目录信息
     *
     * @param userId
     */
    private XPanUserFile getUserRootFileInfo(Long userId) {
        return xPanUserFileService.getUserRootFile(userId);

    }

    /**
     * 创建用户的根目录信息
     */
    private void createUserRootFolder(UserRegisterContext userRegisterContext) {
        CreateFolderContext createFolderContext = new CreateFolderContext();
        createFolderContext.setParentId(FileConstants.TOP_PARENT_ID);
        createFolderContext.setUserId(userRegisterContext.getEntity().getUserId());
        createFolderContext.setFolderName(FileConstants.ALL_FILE_CN_STR);
        xPanUserFileService.createFolder(createFolderContext);
    }

    /**
     * 实现注册用户的业务
     * 需要捕获数据库的唯一索引冲突异常，来实现全局用户名称唯一
     */
    private void doRegister(UserRegisterContext userRegisterContext) {
        XPanUser entity = userRegisterContext.getEntity();
        if (Objects.nonNull(entity)) {
            try {
                if (!save(entity)) {
                    throw new XPanBusinessException("用户注册失败!");
                }
            } catch (DuplicateKeyException duplicateKeyException) {
                throw new XPanBusinessException("用户名存在！");
            }
            return;
        }
        throw new XPanBusinessException(ResponseCode.ERROR);
    }


    /**
     * 实体转化
     * 由上下文信息转化成用户实体，封装进上下文
     */
    private void assembleUserEntity(UserRegisterContext userRegisterContext) {
        XPanUser entity = userConverter.userRegisterContext2RPanUser(userRegisterContext);
        String salt = PasswordUtil.getSalt();
        String dbPassword = PasswordUtil.encryptPassword(salt, userRegisterContext.getPassword());
        entity.setUserId(IdUtil.get());
        entity.setSalt(salt);
        entity.setPassword(dbPassword);
        entity.setCreateTime(LocalDate.now());
        entity.setUpdateTime(LocalDate.now());
        userRegisterContext.setEntity(entity);
    }

    /**
     * 校验用户名密码
     */
    private void checkLoginInfo(UserLoginContext userLoginContext) {
        String username = userLoginContext.getUsername();
        String password = userLoginContext.getPassword();
        XPanUser entity = getRPanUserByUsername(username);
        if (Objects.isNull(entity)) {
            throw new XPanBusinessException("用户名称不存在");
        }
        String salt = entity.getSalt();
        String encPassword = PasswordUtil.encryptPassword(salt, password);
        String dbPassword = entity.getPassword();
        if (!Objects.equals(encPassword, dbPassword)) {
            throw new XPanBusinessException("密码信息不正确");
        }
        userLoginContext.setEntity(entity);
    }

    /**
     * 通过用户名称获取用户实体信息
     */
    private XPanUser getRPanUserByUsername(String username) {
        QueryWrapper<XPanUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }

    /**
     * 生成并保存登陆之后的凭证
     */
    private void generateAndSaveAccessToken(UserLoginContext userLoginContext) {
        XPanUser entity = userLoginContext.getEntity();
        String accessToken = JwtUtil.generateToken(entity.getUsername(), UserConstants.LOGIN_USER_ID, entity.getUserId(), UserConstants.ONE_DAY_LONG);
        Cache cache = cacheManager.getCache(CacheConstants.X_PAN_CACHE_NAME);
        cache.put(UserConstants.USER_LOGIN_PREFIX + entity.getUserId(), accessToken);
        userLoginContext.setAccessToken(accessToken);
    }

    /**
     * 验证忘记密码的token是否有效
     */
    private void checkForgetPasswordToken(ResetPasswordContext resetPasswordContext) {
        String token = resetPasswordContext.getToken();
        Object value = JwtUtil.analyzeToken(token, UserConstants.FORGET_USERNAME);
        if (Objects.isNull(value)) {
            throw new XPanBusinessException(ResponseCode.TOKEN_EXPIRE);
        }
        String tokenUsername = String.valueOf(value);
        if (!Objects.equals(tokenUsername, resetPasswordContext.getUsername())) {
            throw new XPanBusinessException("token错误");
        }
    }

    /**
     * 校验用户信息并重置用户密码
     */
    private void checkAndResetUserPassword(ResetPasswordContext resetPasswordContext) {
        String username = resetPasswordContext.getUsername();
        String password = resetPasswordContext.getPassword();
        XPanUser entity = getRPanUserByUsername(username);
        if (Objects.isNull(entity)) {
            throw new XPanBusinessException("用户信息不存在");
        }

        String newDbPassword = PasswordUtil.encryptPassword(entity.getSalt(), password);
        entity.setPassword(newDbPassword);
        entity.setUpdateTime(LocalDate.now());

        if (!updateById(entity)) {
            throw new XPanBusinessException("重置用户密码失败");
        }
    }

    /**
     * 校验用户的旧密码
     * 改不周会查询并封装用户的实体信息到上下文对象中
     */
    private void checkOldPassword(ChangePasswordContext changePasswordContext) {
        Long userId = changePasswordContext.getUserId();
        String oldPassword = changePasswordContext.getOldPassword();
        XPanUser entity = getById(userId);
        if (Objects.isNull(entity)) {
            throw new XPanBusinessException("用户信息不存在");
        }
        changePasswordContext.setEntity(entity);

        String encOldPassword = PasswordUtil.encryptPassword(entity.getSalt(), oldPassword);
        String dbOldPassword = entity.getPassword();
        if (!Objects.equals(encOldPassword, dbOldPassword)) {
            throw new XPanBusinessException("旧密码不正确");
        }
    }

    /**
     * 修改新密码
     */
    private void doChangePassword(ChangePasswordContext changePasswordContext) {
        String newPassword = changePasswordContext.getNewPassword();
        XPanUser entity = changePasswordContext.getEntity();
        String salt = entity.getSalt();

        String encNewPassword = PasswordUtil.encryptPassword(salt, newPassword);

        entity.setPassword(encNewPassword);

        if (!updateById(entity)) {
            throw new XPanBusinessException("修改用户密码失败");
        }
    }

    /**
     * 退出用户的登录状态
     */
    private void exitLoginStatus(ChangePasswordContext changePasswordContext) {
        exit(changePasswordContext.getUserId());
    }


    @Override
    public XPanUser getById(Serializable id) {
        return userCacheService.getById(id);
//        return super.getById(id);
    }

    @Override
    public List<XPanUser> listByIds(Collection<? extends Serializable> idList) {
        throw new XPanBusinessException("请更换手动缓存!");
//        return super.listByIds(idList);
    }

    /**
     * 根据主键删除ID
     * @param id
     * @return
     */
    @Override
    public boolean removeById(Serializable id) {
//        return super.removeById(id);
        return userCacheService.removeById(id);
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        throw new XPanBusinessException("请更换手动缓存!");
//        return super.removeByIds(idList);
    }

    @Override
    public boolean updateById(XPanUser entity) {
        return userCacheService.updateById(entity.getUserId(),entity);
//        return super.updateById(entity);
    }

    @Override
    public boolean updateBatchById(Collection<XPanUser> entityList) {
        throw new XPanBusinessException("请更换手动缓存!");
//        return super.updateBatchById(entityList);
    }
}




