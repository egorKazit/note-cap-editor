package com.yk.capper.service;

import cds.gen.capper.db.Attachment;
import cds.gen.capper.db.Attachment_;
import cds.gen.capperservice.CapperService_;
import cds.gen.capper.db.Thread;
import com.sap.cds.ql.cqn.CqnComparisonPredicate;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.cds.CdsDeleteEventContext;
import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.cds.CdsService;
import com.sap.cds.services.cds.CdsUpdateEventContext;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.yk.capper.repository.PersistenceServiceWrapper;
import com.yk.common.external.ExternalDriveOperator;
import lombok.extern.log4j.Log4j2;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@ServiceName(CapperService_.CDS_NAME)
@Log4j2
public class AttachmentService implements EventHandler {

    private final ExternalDriveOperator googleDriveOperator;
    private final PersistenceServiceWrapper<Attachment> attachmentPersistenceServiceWrapper;
    private final PersistenceServiceWrapper<Thread> threadPersistenceServiceWrapper;

    public AttachmentService(@Qualifier("google") ExternalDriveOperator googleDriveOperator,
                             @Qualifier("attachment") PersistenceServiceWrapper<Attachment> attachmentPersistenceServiceWrapper,
                             @Qualifier("thread") PersistenceServiceWrapper<Thread> threadPersistenceServiceWrapper) {
        this.googleDriveOperator = googleDriveOperator;
        this.attachmentPersistenceServiceWrapper = attachmentPersistenceServiceWrapper;
        this.threadPersistenceServiceWrapper = threadPersistenceServiceWrapper;
    }

    @On(event = CdsService.EVENT_UPDATE, entity = Attachment_.CDS_NAME)
    public Attachment onAttachmentsUpload(CdsUpdateEventContext context, @NotNull Attachment attachment) throws IOException {
        MimeTypes allMimeTypes = MimeTypes.getDefaultMimeTypes();
        MimeType mimeType;
        try {
            mimeType = allMimeTypes.forName(attachment.getContentType());
        } catch (MimeTypeException e) {
            e.printStackTrace();
            return null;
        }

        InputStream inputStream = attachment.getContent();
        if (inputStream == null)
            return null;

        String name = "thread-attachment-" + UUID.randomUUID() + mimeType.getExtension();
        String fileId = googleDriveOperator.create(name, mimeType.getName(), inputStream);

        Thread thread = threadPersistenceServiceWrapper.readEntry(attachment.getThreadId());
        if (thread != null) {
            thread.setStatus(3);
            threadPersistenceServiceWrapper.update(List.of(thread));
        }

        attachment.setSynchronizationId(fileId);
        attachment.setContent(null);
        attachmentPersistenceServiceWrapper.update(List.of(attachment));
        context.setResult(List.of(attachment));
        context.setCompleted();
        return attachment;
    }

    @After(event = CdsService.EVENT_READ, entity = Attachment_.CDS_NAME)
    public void onAttachmentsRead(@NotNull CdsReadEventContext context, @NotNull Attachment attachment) throws IOException {
        String attachmentId = attachment.getId() != null ? attachment.getId() :
                (String) ((CqnComparisonPredicate) context.getCqn().asSelect().from().asRef().targetSegment().filter()
                        .orElseThrow(() -> new ServiceException("Not found"))
                        .asPredicate())
                        .right().asLiteral().value();
        Attachment sourceAttachment = attachmentPersistenceServiceWrapper.readEntry(attachmentId);
        if (sourceAttachment.getSynchronizationId() == null)
            return;
        InputStream fileInputStream = googleDriveOperator.read(sourceAttachment.getSynchronizationId());
        attachment.setContent(fileInputStream);
    }

    @Before(event = CdsService.EVENT_DELETE, entity = cds.gen.capperservice.Attachment_.CDS_NAME)
    public void onAttachmentDelete(@NotNull CdsDeleteEventContext eventContext) {
        var keysToBeRemoved = eventContext.getCqn().asDelete().ref().segments().stream().map(segment -> segment.filter().orElse(null))
                .filter(Objects::nonNull).map(cqnPredicate -> ((CqnComparisonPredicate) cqnPredicate).right().asLiteral().value().toString()).toList();
        List<Attachment> attachments = attachmentPersistenceServiceWrapper.readEntries(keysToBeRemoved);
        attachments.forEach(attachment -> {
            if (attachment.getSynchronizationId() != null) {
                try {
                    googleDriveOperator.delete(attachment.getSynchronizationId());
                } catch (IOException e) {
                    log.atError().log("Error on file deletion. {}", e.getMessage());
                }
            }
        });
    }


}
