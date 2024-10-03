package com.xiaoxin.pan.server.modules.share.mapper;

import com.xiaoxin.pan.server.modules.share.entity.XPanShare;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoxin.pan.server.modules.share.vo.XPanShareUrlListVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author xiaoxin
* @description 针对表【x_pan_share(用户分享表)】的数据库操作Mapper
* @createDate 2024-07-26 12:50:31
* @Entity com.xiaoxin.pan.server.modules.share.entity.XPanShare
*/
@Mapper
public interface XPanShareMapper extends BaseMapper<XPanShare> {


    /**
     * 查询用户的分享列表
     *
     * @param userId
     * @return
     */
    List<XPanShareUrlListVO> selectShareVOListByUserId(Long userId);
}




