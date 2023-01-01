package com.yk.capper.listener;

import cds.gen.capper.db.Note;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NoteSynchronizationEvent extends ApplicationEvent {

    private final Note note;

    public NoteSynchronizationEvent(Object source, Note note) {
        super(source);
        this.note = note;
    }

}
