package com.xiaoxin.pan.server.modules.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.core.response.ResponseCode;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.core.utils.PasswordUtil;
import com.xiaoxin.pan.server.modules.file.constants.FileConstants;
import com.xiaoxin.pan.server.modules.file.context.CreateFolderContext;
import com.xiaoxin.pan.server.modules.file.service.XPanUserFileService;
import com.xiaoxin.pan.server.modules.user.context.UserRegisterContext;
import com.xiaoxin.pan.server.modules.user.converter.UserConverter;
import com.xiaoxin.pan.server.modules.user.entity.XPanUser;
import com.xiaoxin.pan.server.modules.user.service.XPanUserService;
import com.xiaoxin.pan.server.modules.user.mapper.XPanUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
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
     * 创建用户的根目录信息
     *
     * @param userRegisterContext
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
     *
     * @param userRegisterContext
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
     *
     * @param userRegisterContext
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
}




