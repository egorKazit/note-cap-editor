package com.yk.capper.repository;

import cds.gen.capper.db.Note;
import cds.gen.capper.db.Note_;
import com.sap.cds.services.persistence.PersistenceService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("note")
public class PersistenceServiceWrapperNote extends PersistenceServiceWrapperAbstract<Note_, Note> {

    public PersistenceServiceWrapperNote(PersistenceService persistenceService) {
        super(persistenceService, Note.class, Note_.class);
    }
}
