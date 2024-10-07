package com.xiaoxin.pan.server.modules.share.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class CheckShareCodePO implements Serializable {
    private static final long serialVersionUID = -2866032735275037879L;

    @ApiModelProperty(value = "分享的ID", required = true)
    @NotBlank(message = "分享ID不能为空")
    private String shareId;

    @ApiModelProperty(value = "分享的分享码", required = true)
    @NotBlank(message = "分享的分享码不能为空")
    private String shareCode;
}
