package com.xiaoxin.pan.server.modules.share.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.xiaoxin.pan.serializer.Date2StringSerializer;
import com.xiaoxin.pan.serializer.IdEncryptSerializer;
import com.xiaoxin.pan.server.modules.file.vo.XPanUserFileVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@ApiModel("分享详情的返回实体对象")
@Data
public class ShareDetailVO implements Serializable {
    private static final long serialVersionUID = -1104072487104098622L;

    @JsonSerialize(using = IdEncryptSerializer.class)
    @ApiModelProperty("分享的ID")
    private Long shareId;

    @ApiModelProperty("分享的名称")
    private String shareName;

    @ApiModelProperty("分享的创建时间")
    @JsonSerialize(using = Date2StringSerializer.class)
    private Date createTime;

    @ApiModelProperty("分享的过期类型")
    private Integer shareDay;

    @ApiModelProperty("分享的截止时间")
    @JsonSerialize(using = Date2StringSerializer.class)
    private Date shareEndTime;

    @ApiModelProperty("分享的文件列表")
    private List<XPanUserFileVO> xPanUserFileVOList;

    @ApiModelProperty("分享者的信息")
    private ShareUserInfoVO shareUserInfoVO;
}
