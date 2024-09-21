package com.xiaoxin.pan.server.modules.file.mapper;

import com.xiaoxin.pan.server.modules.file.context.QueryFileListContext;
import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author xiaoxin
* @description 针对表【x_pan_user_file(用户文件信息表)】的数据库操作Mapper
* @createDate 2024-07-26 12:48:16
* @Entity com.xiaoxin.pan.server.modules.file.entity.XPanUserFile
*/
@Mapper
public interface XPanUserFileMapper extends BaseMapper<XPanUserFile> {

    List<XPanUserFileVO> selectFileList(@Param("param") QueryFileListContext context);
}




