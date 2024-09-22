package com.xiaoxin.pan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.core.utils.FileUtils;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.common.envent.file.DeleteFileEvent;
import com.xiaoxin.pan.server.modules.file.constants.FileConstants;
import com.xiaoxin.pan.server.modules.file.context.*;
import com.xiaoxin.pan.server.modules.file.converter.FileConverter;
import com.xiaoxin.pan.server.modules.file.enmus.DelFlagEnum;
import com.xiaoxin.pan.server.modules.file.entity.XPanFile;
import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.xiaoxin.pan.server.modules.file.enums.FileTypeEnum;
import com.xiaoxin.pan.server.modules.file.service.XPanFileService;
import com.xiaoxin.pan.server.modules.file.service.XPanUserFileService;
import com.xiaoxin.pan.server.modules.file.mapper.XPanUserFileMapper;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import com.xiaoxin.pan.server.modules.file.enmus.FolderFlagEnum;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xiaoxin
 * @description 针对表【x_pan_user_file(用户文件信息表)】的数据库操作Service实现
 * @createDate 2024-07-26 12:48:16
 */
@Service
public class XPanUserFileServiceImpl extends ServiceImpl<XPanUserFileMapper, XPanUserFile>
        implements XPanUserFileService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private XPanFileService xPanFileService;
    @Autowired
    private FileConverter fileConverter;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

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
     * 查询用户的根文件夹信息
     *
     * @param userId 用户ID
     * @return 返回用户文件信息
     */
    @Override
    public XPanUserFile getUserRootFile(Long userId) {
        QueryWrapper<XPanUserFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("parent_id", FileConstants.TOP_PARENT_ID);
        queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
        queryWrapper.eq("folder_flag", FolderFlagEnum.YES.getCode());
        return getOne(queryWrapper);
    }

    /**
     * 查询用户的文件列表
     *
     * @param context
     * @return
     */
    @Override
    public List<XPanUserFileVO> getFileList(QueryFileListContext context) {
        return baseMapper.selectFileList(context);
    }


    /**
     * 更新文件名称
     * 1、校验更新文件名称的条件
     * 2、执行更新文件名称的操作
     *
     * @param updateFilenameContext 更新文件名称的参数对象
     */
    @Override
    public void updateFileName(UpdateFilenameContext updateFilenameContext) {
        checkUpdateFilenameCondition(updateFilenameContext);
        doUpdateFilename(updateFilenameContext);
    }

    /**
     * 批量删除文件
     * 1、校验删除的条件
     * 2、执行批量删除的动作
     * 3、发布批量删除文件的事件，给其他模块订阅使用
     */
    @Override
    public void deleteFile(DeleteFileContext deleteFileContext) {
        checkFileDeleteCondition(deleteFileContext);
        doDeleteFile(deleteFileContext);
        afterFileDelete(deleteFileContext);
    }

    /**
     * 文件秒传功能
     * 1、判断用户之前是否上传过该文件
     * 2、如果上传过该文件，只需要生成一个该文件和当前用户在指定文件夹下面的关联关系即可
     *
     * @param uploadFileContext
     * @return true 代表用户之前上传过相同文件并成功挂在了关联关系 false 用户没有上传过该文件，请手动执行上传逻辑
     */
    @Override
    public boolean secUpload(UploadFileContext uploadFileContext) {
        List<XPanFile> fileList = getFileListByUserIdAndIdentifier(uploadFileContext.getUserId(), uploadFileContext.getIdentifier());
        if (CollectionUtils.isNotEmpty(fileList)) {
            XPanFile record = fileList.get(XPanConstants.ZERO_INT);
            saveUserFile(uploadFileContext.getParentId(),
                    uploadFileContext.getFilename(),
                    FolderFlagEnum.NO,
                    FileTypeEnum.getFileTypeCode(FileUtils.getFileSuffix(uploadFileContext.getFilename())),
                    record.getFileId(),
                    uploadFileContext.getUserId(),
                    record.getFileSizeDesc());
            return true;
        }
        return false;
    }

    /**
     * 单文件上传
     * 1、上传文件并保存实体文件的记录
     * 2、保存用户文件的关系记录
     * @param fileUploadContext
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void upload(FileUploadContext fileUploadContext) {
        saveFile(fileUploadContext);
        saveUserFile(fileUploadContext.getParentId(),
                fileUploadContext.getFilename(),
                FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtils.getFileSuffix(fileUploadContext.getFilename())),
                fileUploadContext.getRecord().getFileId(),
                fileUploadContext.getUserId(),
                fileUploadContext.getRecord().getFileSizeDesc());
    }

    /**
     * 上传文件并保存实体文件记录
     * 委托给实体文件的Service去完成该操作
     *
     * @param fileUploadContext
     */
    private void saveFile(FileUploadContext fileUploadContext) {
        FileSaveContext fileSaveContext = fileConverter.fileUploadContext2FileSaveContext(fileUploadContext);
        xPanFileService.saveFile(fileSaveContext);
        fileUploadContext.setRecord(fileSaveContext.getRecord());
    }


    private List<XPanFile> getFileListByUserIdAndIdentifier(Long userId, String identifier) {
        QueryRealFileListContext queryRealFileListContext = new QueryRealFileListContext();
        queryRealFileListContext.setUserId(userId);
        queryRealFileListContext.setIdentifier(identifier);
        return xPanFileService.getFileList(queryRealFileListContext);

    }


    /**
     * 删除文件后置处理
     * 对外发布文件删除的事件
     *
     * @param deleteFileContext
     */
    private void afterFileDelete(DeleteFileContext deleteFileContext) {
        DeleteFileEvent deleteFileEvent = new DeleteFileEvent(this, deleteFileContext.getFileIdList());
        applicationContext.publishEvent(deleteFileEvent);
    }

    /**
     * 删除文件操作
     *
     * @param deleteFileContext
     */
    private void doDeleteFile(DeleteFileContext deleteFileContext) {
        List<Long> fileIdList = deleteFileContext.getFileIdList();
        UpdateWrapper<XPanUserFile> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("file_id", fileIdList);
        updateWrapper.set("del_flag", DelFlagEnum.YES.getCode());
        updateWrapper.set("update_time", new Date());
        if (!update(updateWrapper)) {
            throw new XPanBusinessException("文件删除失败");
        }
    }

    /**
     * 删除文件之前的前置校验
     * 1、文件ID合法校验
     * 2、用户拥有删除该文件的权限
     */
    private void checkFileDeleteCondition(DeleteFileContext deleteFileContext) {
        List<Long> fileIdList = deleteFileContext.getFileIdList();
        List<XPanUserFile> xPanUserFiles = listByIds(fileIdList);
        if (xPanUserFiles.size() != fileIdList.size()) {
            throw new XPanBusinessException("存在不合法的文件记录");
        }
        Set<Long> fileIdSet = xPanUserFiles.stream().map(XPanUserFile::getFileId).collect(Collectors.toSet());
        int oldSize = fileIdSet.size();
        fileIdSet.addAll(fileIdList);
        int newSize = fileIdSet.size();
        if (newSize != oldSize) {
            throw new XPanBusinessException("存在不合法的文件记录");
        }
        Set<Long> userIdSet = xPanUserFiles.stream().map(XPanUserFile::getUserId).collect(Collectors.toSet());
        if (userIdSet.size() != 1) {
            throw new XPanBusinessException("存在不合法的文件记录");
        }
        Long userId = userIdSet.stream().findFirst().get();
        if (!Objects.equals(userId, deleteFileContext.getUserId())) {
            throw new XPanBusinessException("当前登录用户没有删除该文件的权限");
        }
    }

    /**
     * 执行文件重命名的操作
     */
    private void doUpdateFilename(UpdateFilenameContext updateFilenameContext) {
        XPanUserFile xPanUserFile = updateFilenameContext.getEntity();
        xPanUserFile.setFilename(updateFilenameContext.getNewFilename());
        xPanUserFile.setUpdateUser(updateFilenameContext.getUserId());
        xPanUserFile.setUpdateTime(new Date());
        if (!updateById(xPanUserFile)) {
            throw new XPanBusinessException("文件重命名失败！");
        }
    }

    /**
     * 更新文件名称的条件校验
     * 1、文件ID是有效的
     * 2、用户有权限更新该文件的文件名称
     * 3、新旧文件名称不能一样
     * 4、不能使用当前文件夹下面的子文件的名称
     */
    private void checkUpdateFilenameCondition(UpdateFilenameContext updateFilenameContext) {
        Long fileId = updateFilenameContext.getFileId();
        XPanUserFile xPanUserFile = getById(fileId);
        if (Objects.isNull(xPanUserFile)) {
            throw new XPanBusinessException("该文件ID无效");
        }
        if (!Objects.equals(xPanUserFile.getUserId(), updateFilenameContext.getUserId())) {
            throw new XPanBusinessException("当前登录用户没有修改该文件名称的权限");
        }
        if (Objects.equals(xPanUserFile.getFilename(), updateFilenameContext.getNewFilename())) {
            throw new XPanBusinessException("新旧文件名称一致，无需更新");
        }
        QueryWrapper<XPanUserFile> xPanUserFileQueryWrapper = new QueryWrapper<>();
        xPanUserFileQueryWrapper.eq("parent_id", xPanUserFile.getParentId());
        xPanUserFileQueryWrapper.eq("filename", updateFilenameContext.getNewFilename());
        if (count(xPanUserFileQueryWrapper) > 0) {
            throw new XPanBusinessException("改文件名已被占用");
        }
        updateFilenameContext.setEntity(xPanUserFile);
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




