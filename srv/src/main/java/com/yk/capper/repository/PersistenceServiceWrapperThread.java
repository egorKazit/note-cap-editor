package com.yk.capper.repository;

import cds.gen.capper.db.Thread;
import cds.gen.capper.db.Thread_;
import com.sap.cds.services.persistence.PersistenceService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("thread")
public class PersistenceServiceWrapperThread extends PersistenceServiceWrapperAbstract<Thread_, Thread> {

    public PersistenceServiceWrapperThread(PersistenceService persistenceService) {
        super(persistenceService, Thread.class, Thread_.class);
    }

}
