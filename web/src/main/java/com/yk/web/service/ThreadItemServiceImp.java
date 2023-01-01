package com.yk.web.service;

import com.yk.common.service.CommonServiceException;
import com.yk.common.service.HttpOperatorService;
import com.yk.web.vdm.ThreadItem;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class ThreadItemServiceImp implements ThreadItemService {


    private final HttpOperatorService httpOperatorService;

    private final List<ThreadItem> threadItems = new ArrayList<>();

    public ThreadItemServiceImp(HttpOperatorService httpOperatorService) {
        this.httpOperatorService = httpOperatorService;
    }

    @Override
    public ThreadItem readItem(String uuid) throws CommonServiceException, IOException, InterruptedException, URISyntaxException {
        try {
            return httpOperatorService.send("/Note(%s)",
                            HttpMethod.GET.name(),
                            null,
                            ThreadItem.class, 200, MediaType.APPLICATION_JSON_VALUE, null, uuid)
                    .toBuilder().type(ThreadItem.Type.NOTE).build();
        } catch (CommonServiceException | IOException | InterruptedException e) {
            return httpOperatorService.send("Attachment(%s)",
                            HttpMethod.GET.name(),
                            null,
                            ThreadItem.class, 200, MediaType.APPLICATION_JSON_VALUE, null, uuid)
                    .toBuilder().type(ThreadItem.Type.ATTACHMENT).build();
        }
    }

    @Override
    public ThreadItem createNote(String parentUuid, String noteContent) throws CommonServiceException, IOException, InterruptedException, URISyntaxException {
        return httpOperatorService.send("Thread(%s)/notes",
                HttpMethod.POST.name(),
                HttpRequest.BodyPublishers.ofString(new JSONObject().put("content", noteContent).toString()),
                ThreadItem.class, 201, MediaType.APPLICATION_JSON_VALUE, null, parentUuid).toBuilder().type(ThreadItem.Type.NOTE).build();
    }

    @Override
    public ThreadItem updateNote(String uuid, String noteContent) throws CommonServiceException, IOException, InterruptedException, URISyntaxException {
        return httpOperatorService.send("Note(%s)",
                HttpMethod.PUT.name(),
                HttpRequest.BodyPublishers.ofString(new JSONObject().put("content", noteContent).toString()),
                ThreadItem.class, 200, MediaType.APPLICATION_JSON_VALUE, null, uuid).toBuilder().type(ThreadItem.Type.NOTE).build();
    }

    @Override
    public boolean deleteNote(String uuid) throws CommonServiceException, IOException, InterruptedException, URISyntaxException {
        return httpOperatorService.send("Note(%s)", HttpMethod.DELETE.name(), null, uuid) == 204;
    }

    @Override
    public ThreadItem createAttachment(String parentUuid, String sourceName, InputStream inputStream) throws CommonServiceException, IOException, InterruptedException, URISyntaxException {

        ThreadItem threadItem = httpOperatorService.send("Thread(%s)/attachments",
                HttpMethod.POST.name(),
                HttpRequest.BodyPublishers.ofString(new JSONObject().put("name", sourceName).toString()),
                ThreadItem.class, 201, MediaType.APPLICATION_JSON_VALUE, null, parentUuid);

        String mediaType = Files.probeContentType(Path.of(sourceName));
        if (mediaType == null) {
            mediaType = MediaType.TEXT_PLAIN_VALUE;
        }

        httpOperatorService.send("Attachment(%s)/content",
                HttpMethod.PUT.name(),
                HttpRequest.BodyPublishers.ofInputStream(() -> inputStream),
                ThreadItem.class, 204, mediaType, null, threadItem.getID());

        threadItem = httpOperatorService.send("Attachment(%s)",
                HttpMethod.GET.name(),
                null,
                ThreadItem.class, 200, MediaType.APPLICATION_JSON_VALUE, null, threadItem.getID().toString());

        return threadItem.toBuilder().content(threadItem.getName()).type(ThreadItem.Type.ATTACHMENT).build();
    }

    @Override
    public InputStream readAttachment(String uuid) throws CommonServiceException, IOException, InterruptedException, URISyntaxException {
        return httpOperatorService.send("Attachment(%s)/content",
                HttpMethod.GET.name(),
                null,
                200, MediaType.APPLICATION_JSON_VALUE, uuid);
    }

    @Override
    public boolean deleteAttachment(String uuid) throws CommonServiceException, IOException, InterruptedException, URISyntaxException {
        return httpOperatorService.send("Attachment(%s)", HttpMethod.DELETE.name(), null, uuid) == 204;
    }
}
