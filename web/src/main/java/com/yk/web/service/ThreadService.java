package com.yk.web.service;

import com.yk.common.service.CommonServiceException;
import com.yk.web.vdm.Thread;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface ThreadService {

    List<Thread> getAllThreads() throws IOException, InterruptedException, CommonServiceException, URISyntaxException;

    Thread readThread(String id) throws CommonServiceException, IOException, InterruptedException, URISyntaxException;

    Thread createThread(String name) throws IOException, InterruptedException, CommonServiceException, URISyntaxException;

    boolean removeThread(String id) throws IOException, InterruptedException, CommonServiceException, URISyntaxException;

}
