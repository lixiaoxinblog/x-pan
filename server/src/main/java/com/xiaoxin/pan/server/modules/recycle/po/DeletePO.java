package com.xiaoxin.pan.server.modules.recycle.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class DeletePO implements Serializable {
    private static final long serialVersionUID = 7537250280018997096L;

    @ApiModelProperty(value = "要删除的文件ID集合，多个使用公用分割符分隔", required = true)
    @NotBlank(message = "请选择要删除的文件")
    private String fileIds;
}
