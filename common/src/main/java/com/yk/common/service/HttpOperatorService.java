package com.yk.common.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

public interface HttpOperatorService {

    <T> T send(String path, String method, HttpRequest.BodyPublisher bodyPublisher, Class<T> classOfT, int expectedStatus, String contentType, String resultPath, Object... args)
            throws IOException, InterruptedException, CommonServiceException, URISyntaxException;

    InputStream send(String path, String method, HttpRequest.BodyPublisher bodyPublisher, int expectedStatus, String contentType, Object... args)
            throws IOException, InterruptedException, CommonServiceException, URISyntaxException;


    int send(String path, String method, HttpRequest.BodyPublisher bodyPublisher, String... args) throws IOException, InterruptedException, CommonServiceException, URISyntaxException;

}
