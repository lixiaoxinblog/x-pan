package com.xiaoxin.pan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.modules.file.constants.FileConstants;
import com.xiaoxin.pan.server.modules.file.context.CreateFolderContext;
import com.xiaoxin.pan.server.modules.file.enmus.DelFlagEnum;
import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.xiaoxin.pan.server.modules.file.service.XPanUserFileService;
import com.xiaoxin.pan.server.modules.file.mapper.XPanUserFileMapper;
import org.springframework.stereotype.Service;
import com.xiaoxin.pan.server.modules.file.enmus.FolderFlagEnum;

import java.util.Date;

/**
 * @author xiaoxin
 * @description 针对表【x_pan_user_file(用户文件信息表)】的数据库操作Service实现
 * @createDate 2024-07-26 12:48:16
 */
@Service
public class XPanUserFileServiceImpl extends ServiceImpl<XPanUserFileMapper, XPanUserFile>
        implements XPanUserFileService {

    /**
     * 创建文件夹信息
     *
     * @param createFolderContext
     * @return
     */
    @Override
    public Long createFolder(CreateFolderContext createFolderContext) {
        return saveUserFile(createFolderContext.getParentId(),
                createFolderContext.getFolderName(),
                FolderFlagEnum.YES,
                null,
                null,
                createFolderContext.getUserId(),
                null);
    }

    /**
     * 保存用户文件的映射记录
     *
     * @param parentId
     * @param filename
     * @param folderFlagEnum
     * @param fileType       文件类型（1 普通文件 2 压缩文件 3 excel 4 word 5 pdf 6 txt 7 图片 8 音频 9 视频 10 ppt 11 源码文件 12 csv）
     * @param realFileId
     * @param userId
     * @param fileSizeDesc
     * @return
     */
    private Long saveUserFile(Long parentId,
                              String filename,
                              FolderFlagEnum folderFlagEnum,
                              Integer fileType,
                              Long realFileId,
                              Long userId,
                              String fileSizeDesc) {
        XPanUserFile entity = assembleRPanFUserFile(parentId, userId, filename, folderFlagEnum, fileType, realFileId, fileSizeDesc);
        if (!save((entity))) {
            throw new XPanBusinessException("保存文件信息失败");
        }
        return entity.getUserId();
    }

    /**
     * 用户文件映射关系实体转化
     * 1、构建并填充实体
     * 2、处理文件命名一致的问题
     *
     * @param parentId
     * @param userId
     * @param filename
     * @param folderFlagEnum
     * @param fileType
     * @param realFileId
     * @param fileSizeDesc
     * @return
     */
    private XPanUserFile assembleRPanFUserFile(Long parentId, Long userId, String filename, FolderFlagEnum folderFlagEnum, Integer fileType, Long realFileId, String fileSizeDesc) {
        XPanUserFile entity = new XPanUserFile();
        entity.setFileId(IdUtil.get());
        entity.setUserId(userId);
        entity.setParentId(parentId);
        entity.setRealFileId(realFileId);
        entity.setFilename(filename);
        entity.setFolderFlag(folderFlagEnum.getCode());
        entity.setFileSizeDesc(fileSizeDesc);
        entity.setFileType(fileType);
        entity.setDelFlag(DelFlagEnum.NO.getCode());
        entity.setCreateUser(userId);
        entity.setCreateTime(new Date());
        entity.setUpdateUser(userId);
        entity.setUpdateTime(new Date());
        handleDuplicateFilename(entity);
        return entity;
    }

    /**
     * 处理用户重复名称
     * 如果同一文件夹下面有文件名称重复
     * 按照系统级规则重命名文件
     *
     * @param entity
     */
    private void handleDuplicateFilename(XPanUserFile entity) {
        String filename = entity.getFilename(),
                newFilenameWithoutSuffix,
                newFilenameSuffix;
        int newFilenamePointPosition = filename.lastIndexOf(XPanConstants.POINT_STR);
        if (newFilenamePointPosition == XPanConstants.MINUS_ONE_INT) {
            newFilenameWithoutSuffix = filename;
            newFilenameSuffix = StringUtils.EMPTY;
        } else {
            newFilenameWithoutSuffix = filename.substring(XPanConstants.ZERO_INT, newFilenamePointPosition);
            newFilenameSuffix = filename.replace(newFilenameWithoutSuffix, StringUtils.EMPTY);
        }
        int count = getDuplicateFilename(entity, newFilenameWithoutSuffix);
        if (count == 0) {
            return;
        }
        String newFilename = assembleNewFilename(newFilenameWithoutSuffix, count, newFilenameSuffix);
        entity.setFilename(newFilename);
    }

    /**
     * 查找通易付文件夹下面的同名文件数量
     *
     * @param entity
     * @param newFilenameWithoutSuffix
     * @return
     */
    private int getDuplicateFilename(XPanUserFile entity, String newFilenameWithoutSuffix) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("parent_id", entity.getParentId());
        queryWrapper.eq("folder_flag", entity.getFolderFlag());
        queryWrapper.eq("user_id", entity.getUserId());
        queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
        queryWrapper.likeLeft("filename", newFilenameWithoutSuffix);
        return count(queryWrapper);
    }

    /**
     * 拼装新文件名称
     * 拼装规则参考操作系统重复文件名称的重命名规范
     *
     * @param newFilenameWithoutSuffix
     * @param count
     * @param newFilenameSuffix
     * @return
     */
    private String assembleNewFilename(String newFilenameWithoutSuffix, int count, String newFilenameSuffix) {
        String newFilename = new StringBuilder(newFilenameWithoutSuffix)
                .append(FileConstants.CN_LEFT_PARENTHESES_STR)
                .append(count)
                .append(FileConstants.CN_RIGHT_PARENTHESES_STR)
                .append(newFilenameSuffix)
                .toString();
        return newFilename;
    }


}




