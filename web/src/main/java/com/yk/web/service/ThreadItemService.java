package com.yk.web.service;

import com.yk.common.service.CommonServiceException;
import com.yk.web.vdm.ThreadItem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public interface ThreadItemService {

    ThreadItem readItem(String uuid) throws CommonServiceException, IOException, InterruptedException, URISyntaxException;

    ThreadItem createNote(String parentUuid, String noteContent) throws CommonServiceException, IOException, InterruptedException, URISyntaxException;

    ThreadItem updateNote(String uuid, String noteContent) throws CommonServiceException, IOException, InterruptedException, URISyntaxException;

    boolean deleteNote(String uuid) throws CommonServiceException, IOException, InterruptedException, URISyntaxException;

    ThreadItem createAttachment(String parentUuid, String sourceName, InputStream inputStream) throws CommonServiceException, IOException, InterruptedException, URISyntaxException;

    InputStream readAttachment(String uuid) throws CommonServiceException, IOException, InterruptedException, URISyntaxException;

    boolean deleteAttachment(String uuid) throws CommonServiceException, IOException, InterruptedException, URISyntaxException;
}
