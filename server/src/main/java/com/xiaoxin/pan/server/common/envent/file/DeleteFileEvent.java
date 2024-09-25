package com.xiaoxin.pan.server.common.envent.file;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 文件删除事件
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
public class DeleteFileEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1848184715719644846L;
    private List<Long> fileIdList;

    public DeleteFileEvent(Object source, List<Long> fileIdList) {
        super(source);
        this.fileIdList = fileIdList;
    }

}
