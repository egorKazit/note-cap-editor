package com.yk.capper.service;

import cds.gen.capperservice.CapperService_;
import cds.gen.capper.db.Thread;
import cds.gen.capper.db.Thread_;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CdsService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.yk.capper.listener.NoteSynchronizationPublisher;
import com.yk.capper.repository.PersistenceServiceWrapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ServiceName(CapperService_.CDS_NAME)
public final class ThreadService implements EventHandler {

    private final NoteSynchronizationPublisher noteSynchronizationPublisher;
    private final PersistenceServiceWrapper<Thread> threadPersistenceServiceWrapper;

    public ThreadService(NoteSynchronizationPublisher noteSynchronizationPublisher,
                         @Qualifier("thread") PersistenceServiceWrapper<Thread> threadPersistenceServiceWrapper) {
        this.noteSynchronizationPublisher = noteSynchronizationPublisher;
        this.threadPersistenceServiceWrapper = threadPersistenceServiceWrapper;
    }

    @Before(event = {CdsService.EVENT_CREATE, CdsService.EVENT_UPDATE}, entity = Thread_.CDS_NAME)
    public void validate(@NotNull List<Thread> threads) {
        if (threads.stream().anyMatch(note -> "".equals(note.getName())))
            throw new ServiceException(ErrorStatuses.FORBIDDEN, "Note content can not be empty");
    }

    @Before(event = CdsService.EVENT_CREATE, entity = Thread_.CDS_NAME)
    public void setInitialStatus(@NotNull List<Thread> threads) {
        threads.forEach(thread -> thread.setStatus(1));
    }

    @After(event = {CdsService.EVENT_CREATE, CdsService.EVENT_UPDATE}, entity = Thread_.CDS_NAME)
    public void raiseSynchronizationEvent(@NotNull List<Thread> threads) {
        threads.stream().filter(thread -> thread.getNotes() != null).forEach(thread -> thread.getNotes()
                .forEach(noteSynchronizationPublisher::publishNoteSynchronizationEvent));
    }

    @After(event = CdsService.EVENT_CREATE, entity = Thread_.CDS_NAME)
    public void updateStatusOnCreate(@NotNull CdsCreateEventContext context, @NotNull List<Thread> threads) {
        threads.forEach(thread -> thread.setStatus(2));
        threadPersistenceServiceWrapper.update(threads);
        context.setResult(threads);
    }

}
