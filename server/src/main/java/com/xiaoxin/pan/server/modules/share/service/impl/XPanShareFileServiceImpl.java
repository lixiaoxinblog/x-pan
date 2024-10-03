package com.xiaoxin.pan.server.modules.share.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.modules.share.context.SaveShareFilesContext;
import com.xiaoxin.pan.server.modules.share.entity.XPanShareFile;
import com.xiaoxin.pan.server.modules.share.service.XPanShareFileService;
import com.xiaoxin.pan.server.modules.share.mapper.XPanShareFileMapper;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author xiaoxin
 * @description 针对表【x_pan_share_file(用户分享文件表)】的数据库操作Service实现
 * @createDate 2024-07-26 12:50:31
 */
@Service
public class XPanShareFileServiceImpl extends ServiceImpl<XPanShareFileMapper, XPanShareFile>
        implements XPanShareFileService {

    @Override
    public void saveShareFiles(SaveShareFilesContext saveShareFilesContext) {
        Long shareId = saveShareFilesContext.getShareId();
        List<Long> shareFileIdList = saveShareFilesContext.getShareFileIdList();
        ArrayList<XPanShareFile> xPanShareFiles = Lists.newArrayList();
        for (Long id : shareFileIdList) {
            XPanShareFile xPanShareFile = new XPanShareFile();
            xPanShareFile.setId(IdUtil.get());
            xPanShareFile.setShareId(shareId);
            xPanShareFile.setFileId(id);
            xPanShareFile.setCreateTime(new Date());
            xPanShareFile.setCreateUser(saveShareFilesContext.getUserId());
            xPanShareFiles.add(xPanShareFile);
        }
        if (!saveBatch(xPanShareFiles)) {
            throw new XPanBusinessException("保存文件分享关联关系失败");
        }
    }
}




