package com.xiaoxin.pan.server.modules.share.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class CancelSharePO implements Serializable {

    private static final long serialVersionUID = -672593922442505042L;

    @ApiModelProperty(value = "要取消的分享ID的集合，多个使用公用的分割符拼接", required = true)
    @NotBlank(message = "请选择要取消的分享")
    private String shareIds;
}
