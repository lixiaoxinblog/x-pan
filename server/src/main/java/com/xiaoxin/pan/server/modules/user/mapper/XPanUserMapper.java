package com.xiaoxin.pan.server.modules.user.mapper;

import com.xiaoxin.pan.server.modules.user.entity.XPanUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author xiaoxin
* @description 针对表【x_pan_user(用户信息表)】的数据库操作Mapper
* @createDate 2024-07-26 12:51:06
* @Entity com.xiaoxin.pan.server.modules.user.entity.XPanUser
*/
@Mapper
public interface XPanUserMapper extends BaseMapper<XPanUser> {

}




