package com.xiaoxin.pan.server.common.envent.file;

import com.xiaoxin.pan.server.modules.file.entity.XPanUserFile;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
public class FilePhysicalDeleteEvent extends ApplicationEvent {
    private static final long serialVersionUID = -1213307957722924251L;
    /**
     * 所有被物理删除的文件实体集合
     */
    private List<XPanUserFile> allRecords;

    public FilePhysicalDeleteEvent(Object source, List<XPanUserFile> allRecords) {
        super(source);
        this.allRecords = allRecords;
    }
}
