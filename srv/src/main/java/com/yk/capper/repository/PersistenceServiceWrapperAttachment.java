package com.yk.capper.repository;

import cds.gen.capper.db.Attachment;
import cds.gen.capper.db.Attachment_;
import com.sap.cds.services.persistence.PersistenceService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("attachment")
public class PersistenceServiceWrapperAttachment extends PersistenceServiceWrapperAbstract<Attachment_, Attachment> {

    public PersistenceServiceWrapperAttachment(PersistenceService persistenceService) {
        super(persistenceService, Attachment.class, Attachment_.class);
    }

}