package com.xiaoxin.pan.server.modules.file.controller;

import com.google.common.base.Splitter;
import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.response.R;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.common.utils.UserIdUtil;
import com.xiaoxin.pan.server.modules.file.constants.FileConstants;
import com.xiaoxin.pan.server.modules.file.context.CreateFolderContext;
import com.xiaoxin.pan.server.modules.file.context.QueryFileListContext;
import com.xiaoxin.pan.server.modules.file.context.UpdateFilenameContext;
import com.xiaoxin.pan.server.modules.file.converter.FileConverter;
import com.xiaoxin.pan.server.modules.file.enmus.DelFlagEnum;
import com.xiaoxin.pan.server.modules.file.po.CreateFolderPO;
import com.xiaoxin.pan.server.modules.file.po.UpdateFilenamePO;
import com.xiaoxin.pan.server.modules.file.service.XPanUserFileService;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 查询文件列表
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
    public R<List<XPanUserFileVO>> list(@NotBlank(message = "父文件ID不能为空") @RequestParam(value = "parentId" ,required = false) String parentId,
                                        @RequestParam(value = "fileTypes",required = false,defaultValue = FileConstants.ALL_FILE_TYPE) String fileTypes){

        // 解密ID
        Long realParentId = IdUtil.decrypt(parentId);
        List<Integer> fileTypeArray = null;

        if(!Objects.equals(FileConstants.ALL_FILE_TYPE,fileTypes)){
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
    public R<String> createFolder(@Validated  @RequestBody  CreateFolderPO createFolderPO){
        CreateFolderContext createFolderContext = fileConverter.createFolderPO2CreateFolderContext(createFolderPO);
        Long folderId = xPanUserFileService.createFolder(createFolderContext);
        return R.data(IdUtil.encrypt(folderId));
    }

    /**
     * 文件重命名
     */
    @PutMapping("/updateFileName")
    public R updateFileName(@Validated @RequestBody UpdateFilenamePO updateFileNamePO){
        UpdateFilenameContext updateFilenameContext = fileConverter.updateFilenamePO2UpdateFilenameContext(updateFileNamePO);
        xPanUserFileService.updateFileName(updateFilenameContext);
        return R.success();
    }

}
