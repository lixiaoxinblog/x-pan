package com.xiaoxin.pan.server.modules.file.converter;

import com.xiaoxin.pan.server.modules.file.context.*;
import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import com.xiaoxin.pan.server.modules.file.po.*;
import com.xiaoxin.pan.server.modules.file.vo.FolderTreeNodeVO;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import com.xiaoxin.pan.storge.engine.core.context.StoreFileChunkContext;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 文件模块实体转化工具类
 */
@Mapper(componentModel = "spring")
public interface FileConverter {
    @Mapping(target = "parentId", expression = "java(com.xiaoxin.pan.core.utils.IdUtil.decrypt(createFolderPO.getParentId()))")
    @Mapping(target = "userId", expression = "java(com.xiaoxin.pan.server.common.utils.UserIdUtil.get())")
    CreateFolderContext createFolderPO2CreateFolderContext(CreateFolderPO createFolderPO);

    @Mapping(target = "fileId", expression = "java(com.xiaoxin.pan.core.utils.IdUtil.decrypt(updateFilenamePO.getFileId()))")
    @Mapping(target = "userId", expression = "java(com.xiaoxin.pan.server.common.utils.UserIdUtil.get())")
    UpdateFilenameContext updateFilenamePO2UpdateFilenameContext(UpdateFilenamePO updateFilenamePO);

    @Mapping(target = "userId", expression = "java(com.xiaoxin.pan.server.common.utils.UserIdUtil.get())")
    DeleteFileContext deleteFilePO2DeleteFileContext(DeleteFilePO deleteFilePO);

    @Mapping(target = "parentId", expression = "java(com.xiaoxin.pan.core.utils.IdUtil.decrypt(secUploadFilePO.getParentId()))")
    @Mapping(target = "userId", expression = "java(com.xiaoxin.pan.server.common.utils.UserIdUtil.get())")
    UploadFileContext uploadFilePO2UploadFileContext(SecUploadFilePO secUploadFilePO);

    @Mapping(target = "parentId", expression = "java(com.xiaoxin.pan.core.utils.IdUtil.decrypt(fileUploadPO.getParentId()))")
    @Mapping(target = "userId", expression = "java(com.xiaoxin.pan.server.common.utils.UserIdUtil.get())")
    FileUploadContext fileUploadPO2FileUploadContext(FileUploadPO fileUploadPO);

    @Mapping(target = "record", ignore = true)
    FileSaveContext fileUploadContext2FileSaveContext(FileUploadContext context);

    @Mapping(target = "userId", expression = "java(com.xiaoxin.pan.server.common.utils.UserIdUtil.get())")
    FileChunkUploadContext fileChunkUploadPO2FileChunkUploadContext(FileChunkUploadPO fileChunkUploadPO);

    FileChunkSaveContext fileChunkUploadContext2FileChunkSaveContext(FileChunkUploadContext fileChunkUploadContext);

    @Mapping(target = "realPath", ignore = true)
    StoreFileChunkContext fileChunkSaveContext2StoreFileChunkContext(FileChunkSaveContext context);

    @Mapping(target = "userId", expression = "java(com.xiaoxin.pan.server.common.utils.UserIdUtil.get())")
    QueryUploadedChunksContext queryUploadedChunksPO2QueryUploadedChunksContext(QueryUploadedChunksPO queryUploadedChunksPO);

    @Mapping(target = "userId", expression = "java(com.xiaoxin.pan.server.common.utils.UserIdUtil.get())")
    @Mapping(target = "parentId", expression = "java(com.xiaoxin.pan.core.utils.IdUtil.decrypt(fileChunkMergePO.getParentId()))")
    FileChunkMergeContext fileChunkMergePO2FileChunkMergeContext(FileChunkMergePO fileChunkMergePO);

    FileChunkMergeAndSaveContext fileChunkMergeContext2FileChunkMergeAndSaveContext(FileChunkMergeContext context);

    @Mapping(target = "label", source = "record.filename")
    @Mapping(target = "id", source = "record.fileId")
    @Mapping(target = "children", expression = "java(com.google.common.collect.Lists.newArrayList())")
    FolderTreeNodeVO xPanUserFile2FolderTreeNodeVO(XPanUserFile record);

    XPanUserFileVO xPanUserFile2RPanUserFileVO(XPanUserFile record);
}
