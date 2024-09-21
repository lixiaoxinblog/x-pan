package com.xiaoxin.pan.server.modules.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.xiaoxin.pan.serializer.IdEncryptSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class XPanUserVO implements Serializable {

    private static final long serialVersionUID = 821556981609248699L;

    @ApiModelProperty("用户名称")
    private String username;

    @ApiModelProperty("用户根目录的加密ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long rootFileId;

    @ApiModelProperty("用户根目录名称")
    private String rootFilename;

}
