package com.yk.capper.listener;

import cds.gen.capper.db.Note;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteSynchronizationPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishNoteSynchronizationEvent(final Note note) {
        applicationEventPublisher.publishEvent(new NoteSynchronizationEvent(this, note));
    }
}
