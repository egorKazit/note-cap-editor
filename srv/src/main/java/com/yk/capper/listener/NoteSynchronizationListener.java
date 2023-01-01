package com.yk.capper.listener;

import cds.gen.capper.db.Note;
import com.yk.capper.repository.PersistenceServiceWrapper;
import com.yk.common.external.ExternalDriveOperator;
import com.yk.common.propagator.TaskPropagationDriver;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Log4j2
public class NoteSynchronizationListener implements ApplicationListener<NoteSynchronizationEvent> {

    private final PersistenceServiceWrapper<Note> persistenceServiceWrapper;
    private final TaskPropagationDriver<Note> taskPropagationDriver;

    private final ExternalDriveOperator externalDriveOperator;

    public NoteSynchronizationListener(PersistenceServiceWrapper<Note> persistenceServiceWrapper, @Qualifier("google") ExternalDriveOperator externalDriveOperator) {
        this.persistenceServiceWrapper = persistenceServiceWrapper;
        this.externalDriveOperator = externalDriveOperator;
        taskPropagationDriver = TaskPropagationDriver.forType(Note.class).withTaskResolver(this::getFunctionForNote).byIdResolver(Note::getId).build();
    }

    @Override
    public void onApplicationEvent(@NotNull NoteSynchronizationEvent event) {
        try {
            processEvent(event.getNote());
        } catch (IOException | InterruptedException ioException) {
            return;
        }
    }

    private void processEvent(Note note) throws IOException, InterruptedException {
        taskPropagationDriver.propagete(note, incomingNote -> note.getModifiedAt().equals(incomingNote.getModifiedAt()));
    }

    @Contract(pure = true)
    private @NotNull Runnable getFunctionForNote(Note note) {
        return () -> {
            try {
                switch (note.getSynchronizationStatus()) {
                    case 1 -> {
                        String fileId = externalDriveOperator.create(note.getId(), note.getContent());
                        note.setSynchronizationId(fileId);
                    }
                    case 2 -> {
                        var synchronizationId = note.getSynchronizationId() == null ? persistenceServiceWrapper.readEntry(note.getId()).getSynchronizationId() : note.getSynchronizationId();
                        externalDriveOperator.update(synchronizationId, note.getContent());
                    }
                    case 5 -> {
                        var synchronizationId = note.getSynchronizationId() == null ? persistenceServiceWrapper.readEntry(note.getId()).getSynchronizationId() : note.getSynchronizationId();
                        externalDriveOperator.delete(synchronizationId);
                    }
                    default -> {
                    }
                    // do nothing
                }
                note.setSynchronizationStatus(3);
            } catch (IOException ioException) {
                note.setSynchronizationStatus(4);
                log.atError().log(ioException.getMessage());
            }
            persistenceServiceWrapper.update(List.of(note));
        };
    }

}
