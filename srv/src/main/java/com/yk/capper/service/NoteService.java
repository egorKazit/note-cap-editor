package com.yk.capper.service;

import cds.gen.capperservice.CapperService_;
import cds.gen.capper.db.Note;
import cds.gen.capper.db.Note_;
import cds.gen.capper.db.Thread;
import com.sap.cds.ql.cqn.CqnComparisonPredicate;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.EventContext;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.cds.CdsDeleteEventContext;
import com.sap.cds.services.cds.CdsService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.HandlerOrder;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.yk.capper.listener.NoteSynchronizationPublisher;
import com.yk.capper.repository.PersistenceServiceWrapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@ServiceName(CapperService_.CDS_NAME)
public class NoteService implements EventHandler {

    private final NoteSynchronizationPublisher noteSynchronizationPublisher;
    private final PersistenceServiceWrapper<Note> notePersistenceServiceWrapper;
    private final PersistenceServiceWrapper<Thread> threadPersistenceServiceWrapper;

    public NoteService(NoteSynchronizationPublisher noteSynchronizationPublisher,
                       @Qualifier("note") PersistenceServiceWrapper<Note> notePersistenceServiceWrapper,
                       @Qualifier("thread") PersistenceServiceWrapper<Thread> threadPersistenceServiceWrapper) {
        this.noteSynchronizationPublisher = noteSynchronizationPublisher;
        this.notePersistenceServiceWrapper = notePersistenceServiceWrapper;
        this.threadPersistenceServiceWrapper = threadPersistenceServiceWrapper;
    }

    @Before(event = {CdsService.EVENT_CREATE, CdsService.EVENT_UPDATE}, entity = Note_.CDS_NAME)
    public void validate(@NotNull List<Note> notes) {
        if (notes.stream().anyMatch(note -> "".equals(note.getContent())))
            throw new ServiceException(ErrorStatuses.FORBIDDEN, "Note content can not be empty");
    }

    @Before(event = CdsService.EVENT_DELETE, entity = Note_.CDS_NAME)
    public void removeSynchronization(@NotNull CdsDeleteEventContext eventContext) {
        var keysToBeRemoved = eventContext.getCqn().asDelete().ref().segments().stream().map(segment -> segment.filter().orElse(null))
                .filter(Objects::nonNull).map(cqnPredicate -> ((CqnComparisonPredicate) cqnPredicate).right().asLiteral().value().toString()).toList();
        List<Note> notes = notePersistenceServiceWrapper.readEntries(keysToBeRemoved);
        notes.forEach(note -> {
            note.setSynchronizationStatus(5);
            noteSynchronizationPublisher.publishNoteSynchronizationEvent(note);
        });
    }

    @HandlerOrder(1)
    @After(event = CdsService.EVENT_CREATE, entity = Note_.CDS_NAME)
    public void updateThreadStatus(@NotNull List<Note> notes) {
        List<Thread> threads = threadPersistenceServiceWrapper.readEntries(notes.stream().map(Note::getThreadId).toList());
        List<Thread> threadsToUpdate = threads.stream().filter(thread -> thread.getStatus() != 3)
                .peek(thread -> thread.setStatus(3)).toList();
        if (!threadsToUpdate.isEmpty())
            threadPersistenceServiceWrapper.update(threadsToUpdate);
    }

    @HandlerOrder(2)
    @After(event = {CdsService.EVENT_CREATE, CdsService.EVENT_UPDATE, CdsService.EVENT_UPSERT}, entity = Note_.CDS_NAME)
    public List<Note> raiseSynchronizationEvent(EventContext eventContext, @NotNull List<Note> notes) {
        notes.forEach(note -> {
            switch (eventContext.getEvent()) {
                case CdsService.EVENT_CREATE -> note.setSynchronizationStatus(1);
                case CdsService.EVENT_UPDATE, CdsService.EVENT_UPSERT -> note.setSynchronizationStatus(2);
            }
            noteSynchronizationPublisher.publishNoteSynchronizationEvent(note);
        });
        return notes;
    }

}
