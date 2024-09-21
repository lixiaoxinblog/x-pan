package com.xiaoxin.pan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxin.pan.server.modules.file.context.QueryFileListContext;
import com.xiaoxin.pan.server.modules.file.entity.XPanFile;
import com.xiaoxin.pan.server.modules.file.service.XPanFileService;
import com.xiaoxin.pan.server.modules.file.mapper.XPanFileMapper;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
* @author xiaoxin
* @description 针对表【x_pan_file(物理文件信息表)】的数据库操作Service实现
* @createDate 2024-07-26 12:48:16
*/
@Service
public class XPanFileServiceImpl extends ServiceImpl<XPanFileMapper, XPanFile>
    implements XPanFileService{
}




