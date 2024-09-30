package com.xiaoxin.pan.server.common.envent.file;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *文件还原事件实体
 */
@EqualsAndHashCode(callSuper = false)
@ToString
@Getter
@Setter
public class FileRestoreEvent extends ApplicationEvent {

    private static final long serialVersionUID = 3643322157649949164L;
    /**
     * 被成功还原的文件记录ID集合
     */
    private List<Long> fileIdList;


    public FileRestoreEvent(Object source ,List<Long> fileIdList) {
        super(source);
        this.fileIdList = fileIdList;
    }
}
