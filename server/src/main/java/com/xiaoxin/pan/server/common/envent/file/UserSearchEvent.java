package com.xiaoxin.pan.server.common.envent.file;

import com.xiaoxin.pan.server.modules.file.context.FileSearchContext;
import lombok.*;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
public class UserSearchEvent extends ApplicationEvent {

    private static final long serialVersionUID = 3269543190256578887L;

    private FileSearchContext fileSearchContext;

    public UserSearchEvent(Object source,FileSearchContext fileSearchContext) {
        super(source);
        this.fileSearchContext = fileSearchContext;
    }
}
