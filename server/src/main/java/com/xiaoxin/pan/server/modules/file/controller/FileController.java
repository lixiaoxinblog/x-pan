package com.xiaoxin.pan.server.modules.file.controller;

import com.google.common.base.Splitter;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.response.R;
import com.xiaoxin.pan.core.utils.FileUtils;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.common.utils.UserIdUtil;
import com.xiaoxin.pan.server.modules.file.constants.FileConstants;
import com.xiaoxin.pan.server.modules.file.context.*;
import com.xiaoxin.pan.server.modules.file.converter.FileConverter;
import com.xiaoxin.pan.server.modules.file.enums.DelFlagEnum;
import com.xiaoxin.pan.server.modules.file.po.*;
import com.xiaoxin.pan.server.modules.file.service.XPanUserFileService;
import com.xiaoxin.pan.server.modules.file.vo.FileChunkUploadVO;
import com.xiaoxin.pan.server.modules.file.vo.UploadedChunksVO;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import com.xiaoxin.pan.server.modules.user.controller.UserController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件控制器
 */
@RestController
@RequestMapping("file")
@Validated
@Api(tags = "文件模块")
public class FileController {

    @Autowired
    private XPanUserFileService xPanUserFileService;
    @Autowired
    private FileConverter fileConverter;
    @Autowired
    private UserController userController;

    /**
     * 查询文件列表
     *
     * @param parentId
     * @param fileTypes
     * @return
     */
    @ApiOperation(
            value = "查询文件列表",
            notes = "该接口提供了用户插叙某文件夹下面某些文件类型的文件列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/list")
    public R<List<XPanUserFileVO>> list(@NotBlank(message = "父文件ID不能为空") @RequestParam(value = "parentId", required = false) String parentId,
                                        @RequestParam(value = "fileTypes", required = false, defaultValue = FileConstants.ALL_FILE_TYPE) String fileTypes) {

        // 解密ID
        Long realParentId = IdUtil.decrypt(parentId);
        List<Integer> fileTypeArray = null;

        if (!Objects.equals(FileConstants.ALL_FILE_TYPE, fileTypes)) {
            fileTypeArray = Splitter.on(XPanConstants.COMMON_SEPARATOR)
                    .splitToList(fileTypes)
                    .stream().map(Integer::valueOf).collect(Collectors.toList());
        }
        QueryFileListContext queryFileListContext = new QueryFileListContext();
        queryFileListContext.setFileTypeArray(fileTypeArray);
        queryFileListContext.setParentId(realParentId);
        queryFileListContext.setUserId(UserIdUtil.get());
        queryFileListContext.setDelFlag(DelFlagEnum.NO.getCode());
        List<XPanUserFileVO> xPanUserFileVO = xPanUserFileService.getFileList(queryFileListContext);
        return R.data(xPanUserFileVO);
    }

    /**
     * 创建文件夹
     */

    @ApiOperation(
            value = "创建文件夹",
            notes = "该接口提供了创建文件夹的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/createFolder")
    public R<String> createFolder(@Validated @RequestBody CreateFolderPO createFolderPO) {
        CreateFolderContext createFolderContext = fileConverter.createFolderPO2CreateFolderContext(createFolderPO);
        Long folderId = xPanUserFileService.createFolder(createFolderContext);
        return R.data(IdUtil.encrypt(folderId));
    }

    /**
     * 文件重命名
     */
    @ApiOperation(
            value = "文件重命名",
            notes = "该接口提供了文件重命名的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PutMapping("/updateFileName")
    public R updateFileName(@Validated @RequestBody UpdateFilenamePO updateFileNamePO) {
        UpdateFilenameContext updateFilenameContext = fileConverter.updateFilenamePO2UpdateFilenameContext(updateFileNamePO);
        xPanUserFileService.updateFileName(updateFilenameContext);
        return R.success();
    }

    /**
     * 批量删除文件
     */
    @ApiOperation(
            value = "批量删除文件",
            notes = "该接口提供了批量删除文件的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @DeleteMapping()
    public R deleteFile(@Validated @RequestBody DeleteFilePO deleteFilePO) {
        DeleteFileContext deleteFileContext = fileConverter.deleteFilePO2DeleteFileContext(deleteFilePO);
        String fileIds = deleteFilePO.getFileIds();
        List<Long> fileIdList = Splitter.on(XPanConstants.COMMON_SEPARATOR).splitToList(fileIds)
                .stream().map(IdUtil::decrypt).collect(Collectors.toList());
        deleteFileContext.setFileIdList(fileIdList);
        xPanUserFileService.deleteFile(deleteFileContext);
        return R.success();
    }

    /**
     * 文件秒传
     */
    @ApiOperation(
            value = "文件秒传",
            notes = "该接口提供了文件秒传的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/secUpload")
    public R secUpload(@Validated @RequestBody SecUploadFilePO secUploadFilePO) {
        UploadFileContext uploadFileContext = fileConverter.uploadFilePO2UploadFileContext(secUploadFilePO);
        boolean result = xPanUserFileService.secUpload(uploadFileContext);
        if (result) {
            return R.success();
        }
        return R.fail("文件唯一表示不存在，请手动执行文件上传");
    }

    /**
     * 单文件上传
     */
    @ApiOperation(
            value = "单文件上传",
            notes = "该接口提供了单文件上传的功能",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/upload")
    public R upload(@Validated FileUploadPO fileUploadPO) {
        FileUploadContext fileUploadContext = fileConverter.fileUploadPO2FileUploadContext(fileUploadPO);
        //todo 添加文件真实后缀
        String fileExtName = FileUtils.getFileExtName(fileUploadPO.getFile().getOriginalFilename());
        fileUploadContext.setFilename(fileUploadPO.getFilename() + XPanConstants.POINT_STR + fileExtName);
        xPanUserFileService.upload(fileUploadContext);
        return R.success();
    }

    /**
     * 文件分片上传
     */
    @ApiOperation(
            value = "文件分片上传",
            notes = "该接口提供了文件分片上传的功能",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/chunkUpload")
    public R<FileChunkUploadVO> chunkUpload(@Validated FileChunkUploadPO fileChunkUploadPO) {
        FileChunkUploadContext fileChunkUploadContext = fileConverter.fileChunkUploadPO2FileChunkUploadContext(fileChunkUploadPO);
        FileChunkUploadVO fileChunkUploadVO = xPanUserFileService.chunkUpload(fileChunkUploadContext);
        return R.data(fileChunkUploadVO);
    }

    /**
     * 文件分片查询
     */
    @ApiOperation(
            value = "文件分片上传",
            notes = "该接口提供了文件分片上传的功能",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/chunkUpload")
    public R<UploadedChunksVO> getUploadedChunks(@Validated QueryUploadedChunksPO queryUploadedChunksPO) {
        QueryUploadedChunksContext context = fileConverter
                .queryUploadedChunksPO2QueryUploadedChunksContext(queryUploadedChunksPO);
        UploadedChunksVO uploadedChunksVO = xPanUserFileService.getUploadedChunks(context);
        return R.data(uploadedChunksVO);
    }

    @ApiOperation(
            value = "文件分片合并",
            notes = "该接口提供了文件分片合并的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/merge")
    public R mergeFile(@Validated @RequestBody FileChunkMergePO fileChunkMergePO) {
        FileChunkMergeContext context = fileConverter.fileChunkMergePO2FileChunkMergeContext(fileChunkMergePO);
        xPanUserFileService.mergeFile(context);
        return R.success();
    }

    /**
     * 文件下载
     */
    @ApiOperation(
            value = "文件下载",
            notes = "该接口提供了文件下载功能的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/download")
    public void download(@NotBlank(message = "文件ID不能为空") @RequestParam("fileId") String fileId
            , HttpServletResponse httpServletResponse) {
        FileDownloadContext fileDownloadContext = new FileDownloadContext();
        fileDownloadContext.setFileId(IdUtil.decrypt(fileId));
        fileDownloadContext.setResponse(httpServletResponse);
        fileDownloadContext.setUserId(UserIdUtil.get());
        xPanUserFileService.download(fileDownloadContext);
    }

    /**
     * 文件预览
     */
    @ApiOperation(
            value = "文件预览",
            notes = "该接口提供了文件预览功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/preview")
    public void preview(@NotBlank(message = "文件ID不能为空") @RequestParam("fileId") String fileId,
                        HttpServletResponse httpServletResponse) {
        FilePreviewContext filePreviewContext = new FilePreviewContext();
        filePreviewContext.setResponse(httpServletResponse);
        filePreviewContext.setFileId(IdUtil.decrypt(fileId));
        filePreviewContext.setUserId(UserIdUtil.get());
        xPanUserFileService.preview(filePreviewContext);
    }

    /**
     * 播放视频和音频
     */
    @ApiOperation(
            value = "播放音频视频",
            notes = "该接口提供了文件预览功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/player")
    public ResponseEntity<FileSystemResource> playVideoAndAudio(@RequestHeader(value = "Range", required = false) String range,
                                                                @RequestParam("fileId") String fileId,
                                                                HttpServletResponse response) {
        FileRangeContext fileRangeContext = new FileRangeContext();
        fileRangeContext.setFileId(IdUtil.decrypt(fileId));
        fileRangeContext.setResponse(response);
        fileRangeContext.setUserId(UserIdUtil.get());
        fileRangeContext.setRange(range);
        String filePath = xPanUserFileService.playVideoAndAudio(fileRangeContext);
        return ResponseEntity.status(response.getStatus()).body(new FileSystemResource(filePath));
    }
}
