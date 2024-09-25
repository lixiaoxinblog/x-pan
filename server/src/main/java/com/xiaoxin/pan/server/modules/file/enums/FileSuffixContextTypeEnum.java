package com.xiaoxin.pan.server.modules.file.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件后缀对应ContextType类型
 */
@Getter
@AllArgsConstructor
public enum FileSuffixContextTypeEnum {

    JPG(".jpg","image/jpg"),
    PNG(".png","image/png"),
    MP4(".mp4","video/mp4"),
    FLAC(".flac","audio/flac");

    private String fileSuffix;
    private String contextType;

    //根据传入的后缀返回对应的ContentType
    public static String getContextType(String fileSuffix){
        FileSuffixContextTypeEnum[] values = values();
        for (FileSuffixContextTypeEnum value : values) {
            if(value.fileSuffix.equals(fileSuffix)){
                return value.contextType;
            }
        }
        return "";
    }

}
