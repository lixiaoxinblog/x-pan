package com.xiaoxin.pan.core.exception;

import com.xiaoxin.pan.core.response.ResponseCode;
import lombok.Data;

/**
 * 自定义全局业务异常类
 */
@Data
public class XPanBusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    public XPanBusinessException(ResponseCode responseCode) {
        this.code = responseCode.getCode();
        this.message = responseCode.getDesc();
    }

    public XPanBusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public XPanBusinessException(String message) {
        this.code = ResponseCode.ERROR_PARAM.getCode();
        this.message = message;
    }

    public XPanBusinessException() {
        this.code = ResponseCode.ERROR_PARAM.getCode();
        this.message = ResponseCode.ERROR_PARAM.getDesc();
    }

}
