package com.xiaoxin.pan.server.modules.file.converter;

import com.xiaoxin.pan.server.modules.file.context.*;
import com.xiaoxin.pan.server.modules.file.po.*;
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

}
