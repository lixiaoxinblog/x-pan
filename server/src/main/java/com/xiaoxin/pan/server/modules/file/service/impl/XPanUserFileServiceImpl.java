package com.xiaoxin.pan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.sun.deploy.net.HttpUtils;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.exception.XPanBusinessException;
import com.xiaoxin.pan.core.response.R;
import com.xiaoxin.pan.core.utils.FileUtils;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.common.envent.file.DeleteFileEvent;
import com.xiaoxin.pan.server.common.envent.file.UserSearchEvent;
import com.xiaoxin.pan.server.common.utils.HttpUtil;
import com.xiaoxin.pan.server.common.utils.UserIdUtil;
import com.xiaoxin.pan.server.modules.file.constants.FileConstants;
import com.xiaoxin.pan.server.modules.file.context.*;
import com.xiaoxin.pan.server.modules.file.converter.FileConverter;
import com.xiaoxin.pan.server.modules.file.entity.XPanFileChunk;
import com.xiaoxin.pan.server.modules.file.enums.DelFlagEnum;
import com.xiaoxin.pan.server.modules.file.entity.XPanFile;
import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.xiaoxin.pan.server.modules.file.enums.FileTypeEnum;
import com.xiaoxin.pan.server.modules.file.service.XPanFileChunkService;
import com.xiaoxin.pan.server.modules.file.service.XPanFileService;
import com.xiaoxin.pan.server.modules.file.service.XPanUserFileService;
import com.xiaoxin.pan.server.modules.file.mapper.XPanUserFileMapper;
import com.xiaoxin.pan.server.modules.file.vo.*;
import com.xiaoxin.pan.storage.engine.local.LocalStorageEngine;
import com.xiaoxin.pan.storge.engine.core.StorageEngine;
import com.xiaoxin.pan.storge.engine.core.context.ReadFileContext;
import com.xiaoxin.pan.storge.engine.core.context.ReadRangeFileContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import com.xiaoxin.pan.server.modules.file.enums.FolderFlagEnum;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
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
    @Autowired
    private XPanFileChunkService xPanFileChunkService;
    @Autowired
    private StorageEngine storageEngine;

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
     *
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
     * 文件分片上传
     * 1、上传实体文件
     * 2、保存分片文件记录
     * 3、校验是否全部分片上传完成
     *
     * @param fileChunkUploadContext
     * @return
     */
    @Override
    public FileChunkUploadVO chunkUpload(FileChunkUploadContext fileChunkUploadContext) {
        FileChunkSaveContext fileChunkSaveContext = fileConverter.fileChunkUploadContext2FileChunkSaveContext(fileChunkUploadContext);
        xPanFileChunkService.saveChunkFile(fileChunkSaveContext);
        FileChunkUploadVO vo = new FileChunkUploadVO();
        vo.setMergeFlag(fileChunkSaveContext.getMergeFlagEnum().getCode());
        return vo;
    }

    /**
     * 查询用户已上传的分片列表
     * 1、查询已上传的分片列表
     * 2、封装返回实体
     */
    @Override
    public UploadedChunksVO getUploadedChunks(QueryUploadedChunksContext queryUploadedChunksContext) {
        QueryWrapper<XPanFileChunk> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("chunk_number");
        queryWrapper.eq("identifier", queryUploadedChunksContext.getIdentifier());
        queryWrapper.eq("create_user", queryUploadedChunksContext.getUserId());
        queryWrapper.gt("expiration_time", new Date());
        List<Integer> uploadedChunks = xPanFileChunkService.listObjs(queryWrapper, value -> (Integer) value);
        UploadedChunksVO uploadedChunksVO = new UploadedChunksVO();
        uploadedChunksVO.setUploadedChunks(uploadedChunks);
        return uploadedChunksVO;
    }

    /**
     * 文件分片合并
     * 1、文件分片物理合并
     * 2、保存文件实体记录
     * 3、保存文件用户关系映射
     */
    @Override
    public void mergeFile(FileChunkMergeContext fileChunkMergeContext) {
        mergeFileChunkAndSaveFile(fileChunkMergeContext);
        saveUserFile(fileChunkMergeContext.getParentId(),
                fileChunkMergeContext.getFilename(),
                FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtils.getFileSuffix(fileChunkMergeContext.getFilename())),
                fileChunkMergeContext.getRecord().getFileId(),
                fileChunkMergeContext.getUserId(),
                fileChunkMergeContext.getRecord().getFileSizeDesc());
    }

    /**
     * 文件下载
     * 1、参数校验：校验文件是否存在，文件是否属于该用户
     * 2、校验该文件是不是一个文件夹
     * 3、执行下载的动作
     *
     * @param fileDownloadContext
     */
    @Override
    public void download(FileDownloadContext fileDownloadContext) {
        XPanUserFile xPanUserFile = getById(fileDownloadContext.getFileId());
        checkOperatePermission(xPanUserFile, fileDownloadContext.getUserId());
        if (checkIsFolder(xPanUserFile)) {
            throw new XPanBusinessException("文件夹暂不支持下载");
        }
        doDownload(xPanUserFile, fileDownloadContext.getResponse());
    }

    /**
     * 文件预览
     * 1、参数校验：校验文件是否存在，文件是否属于该用户
     * 2、校验该文件是不是一个文件夹
     * 3、执行预览的动作
     *
     * @param filePreviewContext
     */
    @Override
    public void preview(FilePreviewContext filePreviewContext) {
        XPanUserFile xPanUserFile = getById(filePreviewContext.getFileId());
        checkOperatePermission(xPanUserFile, filePreviewContext.getUserId());
        if (checkIsFolder(xPanUserFile)) {
            throw new XPanBusinessException("文件夹暂不支持预览");
        }
        doPreview(xPanUserFile, filePreviewContext);
    }


    /**
     * 音频视频播放
     * 1、参数校验：校验文件是否存在，文件是否属于该用户
     * 2、校验该文件是不是一个文件夹
     * 3、执行播放动作
     *
     * @param fileRangeContext
     */
    @Override
    public String playVideoAndAudio(FileRangeContext fileRangeContext) {
        XPanUserFile xPanUserFile = getById(fileRangeContext.getFileId());
        checkOperatePermission(xPanUserFile, fileRangeContext.getUserId());
        if (checkIsFolder(xPanUserFile)) {
            throw new XPanBusinessException("文件夹暂不支持播放");
        }
        return doPlayer(xPanUserFile, fileRangeContext);
    }

    /**
     * 获取文件树
     * 1、查询出该用户的所有文件夹列表
     * 2、在内存中拼装文件夹树
     *
     * @param queryFolderTreeContext
     * @return
     */
    @Override
    public List<FolderTreeNodeVO> getFolderTree(QueryFolderTreeContext queryFolderTreeContext) {
        List<XPanUserFile> folderRecords = queryFolderRecords(queryFolderTreeContext.getUserId());
        return assembleFolderTreeNodeVOList(folderRecords);
    }

    /**
     * 转移文件
     *
     * @param transferFileContext
     */
    @Override
    public void transfer(TransferFileContext transferFileContext) {
        checkTransferCondition(transferFileContext);
        doTransfer(transferFileContext);
    }

    /**
     * 复制文件
     *
     * @param copyFileContext
     */
    @Override
    public void copy(CopyFileContext copyFileContext) {
        checkCopyCondition(copyFileContext);
        doCopy(copyFileContext);
    }

    /**
     * 搜索文件
     * 1、执行文件搜索
     * 2、拼装文件的父文件夹名称
     * 3、执行文件搜索后的后置动作
     *
     * @param fileSearchContext
     * @return
     */
    @Override
    public List<FileSearchResultVO> search(FileSearchContext fileSearchContext) {
        List<FileSearchResultVO> result = doSearch(fileSearchContext);
        fillParentFilename(result);
        afterSearch(fileSearchContext);
        return result;
    }

    /**
     * 查询面包屑列表
     * 1、获取用户所有文件夹信息
     * 2、拼接需要用到的面包屑的列表
     *
     * @param queryBreadcrumbsContext
     * @return
     */
    @Override
    public List<BreadcrumbVO> getBreadcrumbs(QueryBreadcrumbsContext queryBreadcrumbsContext) {
        List<XPanUserFile> folderRecords = queryFolderRecords(queryBreadcrumbsContext.getUserId());
        Map<Long, BreadcrumbVO> prepareBreadcrumbVOMap = folderRecords.stream()
                .map(BreadcrumbVO::transfer)
                .collect(Collectors.toMap(BreadcrumbVO::getId, r -> r));
        BreadcrumbVO currentNode = null;
        Long fileId = queryBreadcrumbsContext.getFileId();
        List<BreadcrumbVO> result = Lists.newLinkedList();

        do {
            currentNode = prepareBreadcrumbVOMap.get(fileId);
            if (Objects.nonNull(currentNode)) {
                result.add(0, currentNode);
                fileId = currentNode.getParentId();
            }
        } while (Objects.nonNull(currentNode));

        return result;
    }

    /**
     * 递归查询所有子文件记录
     *
     * @param records
     * @return
     */
    @Override
    public List<XPanUserFile> findAllFileRecords(List<XPanUserFile> records) {
        ArrayList<XPanUserFile> xPanUserFiles = Lists.newArrayList(records);
        if (CollectionUtils.isEmpty(xPanUserFiles)) {
            return xPanUserFiles;
        }
        long count = xPanUserFiles
                .stream()
                .filter(record -> Objects.equals(record.getFolderFlag(), FolderFlagEnum.YES.getCode()))
                .count();
        if (count == 0) {
            return xPanUserFiles;
        }
        records.forEach(record -> doFindAllFileRecords(xPanUserFiles, record));
        return records;
    }

    /**
     * 递归查询所有的子文件信息
     *
     * @param xPanUserFiles
     * @param record
     */
    private void doFindAllFileRecords(ArrayList<XPanUserFile> xPanUserFiles, XPanUserFile record) {
        if (Objects.isNull(record)) {
            return;
        }
        if (!checkIsFolder(record)) {
            return;
        }
        List<XPanUserFile> childRecords = findChildRecordsIgnoreDelFlag(record.getFileId());
        if (org.apache.commons.collections.CollectionUtils.isEmpty(childRecords)) {
            return;
        }
        xPanUserFiles.addAll(childRecords);
        childRecords.stream()
                .filter(childRecord -> FolderFlagEnum.YES.getCode().equals(childRecord.getFolderFlag()))
                .forEach(childRecord -> doFindAllFileRecords(xPanUserFiles, childRecord));
    }

    /**
     * 查询文件夹下面的文件记录，忽略删除标识
     *
     * @param fileId
     * @return
     */
    private List<XPanUserFile> findChildRecordsIgnoreDelFlag(Long fileId) {
        QueryWrapper<XPanUserFile> queryWrapper = Wrappers.query();
        queryWrapper.eq("parent_id", fileId);
        return list(queryWrapper);
    }

    /**
     * 查询文件后置处理
     * 发布文件搜索的事件
     *
     * @param fileSearchContext
     */
    private void afterSearch(FileSearchContext fileSearchContext) {
        UserSearchEvent userSearchEvent = new UserSearchEvent(this, fileSearchContext);
        applicationContext.publishEvent(userSearchEvent);
    }

    /**
     * 填充文件搜索结果的父文件夹名称
     *
     * @param result
     */
    private void fillParentFilename(List<FileSearchResultVO> result) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }
        List<Long> parentIdList = new ArrayList<>();
        for (FileSearchResultVO fileSearchResultVO : result) {
            Long parentId = fileSearchResultVO.getParentId();
            parentIdList.add(parentId);
        }
        List<XPanUserFile> parentRecords = listByIds(parentIdList);
        Map<Long, String> fileId2filenameMap = parentRecords.stream()
                .collect(Collectors.toMap(XPanUserFile::getFileId,
                        XPanUserFile::getFilename));
        result.forEach(vo -> vo.setParentFilename(fileId2filenameMap.get(vo.getParentId())));
    }

    /**
     * 执行文件搜索
     *
     * @param fileSearchContext
     * @return
     */
    private List<FileSearchResultVO> doSearch(FileSearchContext fileSearchContext) {
        return baseMapper.selectFile(fileSearchContext);
    }

    /**
     * 执行复制动作
     *
     * @param copyFileContext
     */
    private void doCopy(CopyFileContext copyFileContext) {
        List<XPanUserFile> prepareRecords = copyFileContext.getPrepareRecords();
        if (CollectionUtils.isNotEmpty(prepareRecords)) {
            ArrayList<XPanUserFile> allRecords = Lists.newArrayList();
            prepareRecords.forEach(record -> assembleCopyChildRecord(
                    allRecords
                    , record
                    , copyFileContext.getTargetParentId()
                    , copyFileContext.getUserId()
            ));
            if (!saveBatch(allRecords)) {
                throw new XPanBusinessException("文件复制失败");
            }
        }
    }


    /**
     * 拼装当前文件记录及子文件记录
     *
     * @param allRecords
     * @param record
     * @param targetParentId
     * @param userId
     */
    private void assembleCopyChildRecord(ArrayList<XPanUserFile> allRecords, XPanUserFile record, Long targetParentId, Long userId) {
        Long newFileId = IdUtil.get();
        Long oldFileId = record.getFileId();
        record.setFileId(newFileId);
        record.setParentId(targetParentId);
        record.setUserId(userId);
        record.setCreateTime(new Date());
        record.setCreateUser(userId);
        record.setUpdateTime(new Date());
        record.setUpdateUser(userId);
        handleDuplicateFilename(record);
        allRecords.add(record);

        if (checkIsFolder(record)) {
            List<XPanUserFile> xPanUserFiles = findChildRecords(oldFileId);
            xPanUserFiles.forEach(childRecords -> {
                assembleCopyChildRecord(allRecords, childRecords, newFileId, userId);
            });
        }
    }

    /**
     * 查找下一级的文件记录
     *
     * @param parentId
     * @return
     */
    private List<XPanUserFile> findChildRecords(Long parentId) {
        QueryWrapper<XPanUserFile> queryWrapper = Wrappers.query();
        queryWrapper.eq("parent_id", parentId);
        queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
        return list(queryWrapper);
    }

    /**
     * 文件转移的条件校验
     * <p>
     * 1、目标文件必须是一个文件夹
     * 2、选中的要转移的文件列表中不能含有目标文件夹以及其子文件夹
     */
    private void checkCopyCondition(CopyFileContext copyFileContext) {
        Long targetParentId = copyFileContext.getTargetParentId();
        if (!checkIsFolder(getById(targetParentId))) {
            throw new XPanBusinessException("目标文件不是一个文件夹");
        }
        List<Long> fileIdList = copyFileContext.getFileIdList();
        List<XPanUserFile> xPanUserFiles = listByIds(fileIdList);
        copyFileContext.setPrepareRecords(xPanUserFiles);
        if (checkIsChildFolder(xPanUserFiles, targetParentId, copyFileContext.getUserId())) {
            throw new XPanBusinessException("目标文件夹ID不能是选中文件列表的文件夹ID或其子文件夹ID");
        }
    }

    /**
     * 执行文件转移的动作
     *
     * @param transferFileContext
     */
    private void doTransfer(TransferFileContext transferFileContext) {
        List<XPanUserFile> prepareRecords = transferFileContext.getPrepareRecords();
        prepareRecords.stream().forEach(record -> {
            record.setParentId(transferFileContext.getTargetParentId());
            record.setUserId(transferFileContext.getUserId());
            record.setCreateUser(transferFileContext.getUserId());
            record.setCreateTime(new Date());
            record.setUpdateUser(transferFileContext.getUserId());
            record.setUpdateTime(new Date());
            handleDuplicateFilename(record);
        });
        if (!updateBatchById(prepareRecords)) {
            throw new XPanBusinessException("文件转移失败");
        }
    }

    /**
     * 文件转移的条件校验
     * 1、目标文件必须是一个文件夹
     * 2、选中的要转移的文件列表中不能含有目标文件夹以及其子文件夹
     *
     * @param transferFileContext
     */
    private void checkTransferCondition(TransferFileContext transferFileContext) {
        Long targetParentId = transferFileContext.getTargetParentId();
        if (!checkIsFolder(getById(targetParentId))) {
            throw new XPanBusinessException("目标文件不是一个文件夹");
        }
        List<Long> fileIdList = transferFileContext.getFileIdList();
        List<XPanUserFile> prepareRecords = listByIds(fileIdList);
        transferFileContext.setPrepareRecords(prepareRecords);
        if (checkIsChildFolder(prepareRecords, targetParentId, transferFileContext.getUserId())) {
            throw new XPanBusinessException("目标文件夹ID不能是选中文件列表的文件夹ID或其子文件夹ID");
        }
    }

    /**
     * 校验目标文件夹ID是都是要操作的文件记录的文件夹ID以及其子文件夹ID
     * 1、如果要操作的文件列表中没有文件夹，那就直接返回false
     * 2、拼装文件夹ID以及所有子文件夹ID，判断存在即可
     *
     * @param prepareRecords
     * @param targetParentId
     * @param userId
     * @return
     */
    private boolean checkIsChildFolder(List<XPanUserFile> prepareRecords, Long targetParentId, Long userId) {
        prepareRecords = prepareRecords.stream()
                .filter(record -> Objects.equals(record.getFolderFlag(),
                        FolderFlagEnum.YES.getCode()))
                .collect(Collectors.toList());
        if (org.apache.commons.collections.CollectionUtils.isEmpty(prepareRecords)) {
            return false;
        }
        List<XPanUserFile> folderRecords = queryFolderRecords(userId);
        Map<Long, List<XPanUserFile>> folderRecordMap = folderRecords
                .stream()
                .collect(Collectors.groupingBy(XPanUserFile::getParentId));
        List<XPanUserFile> unavailableFolderRecords = Lists.newArrayList();
        unavailableFolderRecords.addAll(prepareRecords);
        prepareRecords.stream()
                .forEach(record ->
                        findAllChildFolderRecords(unavailableFolderRecords,
                                folderRecordMap, record));
        List<Long> unavailableFolderRecordIds = unavailableFolderRecords
                .stream()
                .map(XPanUserFile::getFileId)
                .collect(Collectors.toList());
        return unavailableFolderRecordIds.contains(targetParentId);
    }

    /**
     * 查找文件夹的所有子文件夹记录
     *
     * @param unavailableFolderRecords
     * @param folderRecordMap
     * @param record
     */
    private void findAllChildFolderRecords(List<XPanUserFile> unavailableFolderRecords,
                                           Map<Long, List<XPanUserFile>> folderRecordMap,
                                           XPanUserFile record) {
        if (Objects.isNull(record)) {
            return;
        }
        List<XPanUserFile> childFolderRecords = folderRecordMap.get(record.getFileId());
        if (org.apache.commons.collections.CollectionUtils.isEmpty(childFolderRecords)) {
            return;
        }
        unavailableFolderRecords.addAll(childFolderRecords);
        childFolderRecords.stream().forEach(childRecord -> findAllChildFolderRecords(unavailableFolderRecords, folderRecordMap, childRecord));
    }

    /**
     * 拼装文件夹树列表
     *
     * @param folderRecords
     * @return
     */
    private List<FolderTreeNodeVO> assembleFolderTreeNodeVOList(List<XPanUserFile> folderRecords) {
        if (CollectionUtils.isEmpty(folderRecords)) {
            return new ArrayList<>();
        }
        List<FolderTreeNodeVO> folderTreeNodeVOList = folderRecords.stream()
                .map(fileConverter::xPanUserFile2FolderTreeNodeVO)
                .collect(Collectors.toList());
        Map<Long, List<FolderTreeNodeVO>> mappedFolderTreeNodeVOMap = folderTreeNodeVOList.stream()
                .collect(Collectors.groupingBy(FolderTreeNodeVO::getParentId));
        for (FolderTreeNodeVO node : folderTreeNodeVOList) {
            List<FolderTreeNodeVO> folderTreeNodeVOS = mappedFolderTreeNodeVOMap.get(node.getId());
            if (CollectionUtils.isNotEmpty(folderTreeNodeVOS)) {
                node.getChildren().addAll(folderTreeNodeVOS);
            }
        }
        return folderTreeNodeVOList;
    }

    /**
     * 查询用户所有有效的文件夹信息
     *
     * @param userId
     * @return
     */
    private List<XPanUserFile> queryFolderRecords(Long userId) {
        QueryWrapper<XPanUserFile> queryWrapper = Wrappers.query();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("folder_flag", FolderFlagEnum.YES.getCode());
        queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
        return list(queryWrapper);
    }

    /**
     * 执行播放动作
     *
     * @param xPanUserFile
     * @param fileRangeContext
     */
    private String doPlayer(XPanUserFile xPanUserFile, FileRangeContext fileRangeContext) {
        XPanFile xPanFile = xPanFileService.getById(xPanUserFile.getRealFileId());
        if (Objects.isNull(xPanFile)) {
            throw new XPanBusinessException("当前文件记录不存在!");
        }
        addRangeResponseHeader(fileRangeContext, xPanFile);
        realFile2OutputStreamRange(xPanFile.getRealPath(), fileRangeContext);
        return xPanFile.getRealPath();
    }


    /**
     * 执行文件预览
     * 1.判断是否是音频视频文件
     * a.音频视频文件请求投设置为Range
     *
     * @param xPanUserFile
     * @param filePreviewContext
     */
    private void doPreview(XPanUserFile xPanUserFile, FilePreviewContext filePreviewContext) {
        XPanFile xPanFile = xPanFileService.getById(xPanUserFile.getRealFileId());
        if (Objects.isNull(xPanFile)) {
            throw new XPanBusinessException("当前文件记录不存在!");
        }
        addCommonResponseHeader(filePreviewContext.getResponse(), xPanFile.getFilePreviewContentType());
        realFile2OutputStream(xPanFile.getRealPath(), filePreviewContext.getResponse());
    }

    /**
     * 委托文件存储引擎去范围读取文件内容并写入到输出流中
     *
     * @param realPath
     * @param fileRangeContext
     */
    private void realFile2OutputStreamRange(String realPath, FileRangeContext fileRangeContext) {
        try {
            ReadRangeFileContext readRangeFileContext = new ReadRangeFileContext();
            readRangeFileContext.setOutputStream(fileRangeContext.getResponse().getOutputStream());
            readRangeFileContext.setRealPath(realPath);
            readRangeFileContext.setStart(fileRangeContext.getStart());
            readRangeFileContext.setEnd(fileRangeContext.getEnd());
            storageEngine.rangeFile(readRangeFileContext);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加Range响应头
     */
    private void addRangeResponseHeader(FileRangeContext fileRangeContext, XPanFile xPanFile) {
        fileRangeContext.getResponse().reset();
        long start, end, fileSize = Long.parseLong(xPanFile.getFileSize());
        String range = fileRangeContext.getRange();
        if (Objects.isNull(range)) {
            // 如果没有指定范围，则从头开始读取整个文件
            start = 0;
            end = fileSize - 1;
            fileRangeContext.getResponse().setStatus(HttpStatus.OK.value());
        } else {
            // 解析 Range 头部信息
            start = Long.parseLong(range.substring(range.indexOf("=") + 1, range.indexOf("-")));
            end = (range.endsWith("-")) ? fileSize - 1 : Long.parseLong(range.substring(range.indexOf("-") + 1));
            fileRangeContext.getResponse().setStatus(HttpStatus.PARTIAL_CONTENT.value());
        }
        // 设置 Content-Range 头
        fileRangeContext.getResponse().setHeader(FileConstants.CONTENT_RANGE_STR, "bytes " + start + "-" + end + "/" + fileSize);
        fileRangeContext.getResponse().setHeader(FileConstants.ACCEPT_RANGES_STR, "bytes");
        fileRangeContext.getResponse().setContentType(xPanFile.getFilePreviewContentType());
        fileRangeContext.setStart(start);
        fileRangeContext.setEnd(end);
    }

    /**
     * 执行下载文件
     *
     * @param xPanUserFile
     * @param response
     */
    private void doDownload(XPanUserFile xPanUserFile, HttpServletResponse response) {
        XPanFile xPanFile = xPanFileService.getById(xPanUserFile.getRealFileId());
        if (Objects.isNull(xPanFile)) {
            throw new XPanBusinessException("当前文件记录不存在!");
        }
        addCommonResponseHeader(response, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        addDownloadAttribute(response, xPanUserFile, xPanFile);
        realFile2OutputStream(xPanFile.getRealPath(), response);
    }

    /**
     * 委托文件存储引擎去读取文件内容并写入到输出流中
     *
     * @param realPath
     * @param response
     */
    private void realFile2OutputStream(String realPath, HttpServletResponse response) {
        try {
            ReadFileContext readFileContext = new ReadFileContext();
            readFileContext.setOutputStream(response.getOutputStream());
            readFileContext.setRealPath(realPath);
            storageEngine.realFile(readFileContext);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加文件下载的属性信息
     *
     * @param response
     * @param xPanUserFile
     * @param xPanFile
     */
    private void addDownloadAttribute(HttpServletResponse response, XPanUserFile xPanUserFile, XPanFile xPanFile) {
        try {
            response.addHeader(FileConstants.CONTENT_DISPOSITION_STR
                    , FileConstants.CONTENT_DISPOSITION_VALUE_PREFIX_STR +
                            new String(xPanUserFile.getFilename().getBytes(FileConstants.GB2312_STR), FileConstants.IOS_8859_1_STR));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new XPanBusinessException("文件下载失败");
        }
        response.setContentLengthLong(Long.parseLong(xPanFile.getFileSize()));
    }

    /**
     * 添加下载响应头
     *
     * @param response
     * @param contentTypeValue
     */
    private void addCommonResponseHeader(HttpServletResponse response, String contentTypeValue) {
        response.reset();
        HttpUtil.addCorsResponseHeaders(response);
        response.addHeader(FileConstants.CONTENT_TYPE_STR, contentTypeValue);
        response.setContentType(contentTypeValue);
    }

    /**
     * 校验文件是否是个文件夹
     *
     * @param xPanUserFile
     * @return
     */

    private boolean checkIsFolder(XPanUserFile xPanUserFile) {
        if (Objects.isNull(xPanUserFile)) {
            throw new XPanBusinessException("当前文件记录不存在!");
        }
        return FolderFlagEnum.YES.getCode().equals(xPanUserFile.getFolderFlag());
    }

    /**
     * 校验文件操作权限
     *
     * @param xPanUserFile
     * @param userId
     */
    private void checkOperatePermission(XPanUserFile xPanUserFile, Long userId) {
        if (Objects.isNull(xPanUserFile)) {
            throw new XPanBusinessException("该文件不存在");
        }
        if (!xPanUserFile.getUserId().equals(userId)) {
            throw new XPanBusinessException("您没有该文件权限！");
        }

    }

    /**
     * 合并文件分片并保存物理文件记录
     */
    private void mergeFileChunkAndSaveFile(FileChunkMergeContext fileChunkMergeContext) {
        FileChunkMergeAndSaveContext fileChunkMergeAndSaveContext = fileConverter
                .fileChunkMergeContext2FileChunkMergeAndSaveContext(fileChunkMergeContext);
        xPanFileService.mergeFileChunkAndSaveFile(fileChunkMergeAndSaveContext);
        fileChunkMergeContext.setRecord(fileChunkMergeAndSaveContext.getRecord());
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
        XPanUserFile entity = assembleXPanFUserFile(parentId, userId, filename, folderFlagEnum, fileType, realFileId, fileSizeDesc);
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
    private XPanUserFile assembleXPanFUserFile(Long parentId, Long userId, String filename, FolderFlagEnum folderFlagEnum, Integer fileType, Long realFileId, String fileSizeDesc) {
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
     * 处理文件重复名称
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




