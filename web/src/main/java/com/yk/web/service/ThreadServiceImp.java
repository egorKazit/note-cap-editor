package com.yk.web.service;

import com.yk.common.service.CommonServiceException;
import com.yk.common.service.HttpOperatorService;
import com.yk.web.vdm.Thread;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.Arrays;
import java.util.List;

@Service
public class ThreadServiceImp implements ThreadService {

    private final HttpOperatorService httpOperatorService;

    public ThreadServiceImp(HttpOperatorService httpOperatorService) {
        this.httpOperatorService = httpOperatorService;
    }

    @Override
    public List<Thread> getAllThreads() throws IOException, InterruptedException, CommonServiceException, URISyntaxException {
        return Arrays.stream(httpOperatorService.send("Thread",
                HttpMethod.GET.name(), null, Thread[].class, 200, MediaType.APPLICATION_JSON_VALUE, "value")).toList();
    }

    @Override
    public Thread createThread(String name) throws IOException, InterruptedException, CommonServiceException, URISyntaxException {
        return httpOperatorService.send("Thread", HttpMethod.POST.name(),
                HttpRequest.BodyPublishers.ofString(new JSONObject().put("name", name).toString()),
                Thread.class, 201, MediaType.APPLICATION_JSON_VALUE, null);
    }

    @Override
    public Thread readThread(String uuid) throws IOException, InterruptedException, CommonServiceException, URISyntaxException {
        return httpOperatorService.send("Thread(%s)?$expand=notes($orderby=modifiedAt desc),attachments($orderby=modifiedAt)",
                HttpMethod.GET.name(), null, Thread.class, 200, MediaType.APPLICATION_JSON_VALUE, null, uuid);
    }

    @Override
    public boolean removeThread(String uuid) throws IOException, InterruptedException, CommonServiceException, URISyntaxException {
        return httpOperatorService.send("Thread(%s)", HttpMethod.DELETE.name(), null, uuid) == 204;
    }

}
