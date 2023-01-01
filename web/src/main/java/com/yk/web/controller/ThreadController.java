package com.yk.web.controller;

import com.yk.common.service.CommonServiceException;
import com.yk.common.utils.JsonConverter;
import com.yk.web.service.ThreadService;
import com.yk.web.vdm.Thread;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;

@Controller
public class ThreadController {

    private final ThreadService threadService;

    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }

    @GetMapping("/threads")
    public ResponseEntity<String> getThreads() {
        try {
            return ResponseEntity.ok(JsonConverter.InstanceHolder.instance.getInstance().convertToJson(threadService.getAllThreads()));
        } catch (IOException | InterruptedException | CommonServiceException | URISyntaxException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/thread")
    public ResponseEntity<String> createThread(@RequestBody String threadRequest) {
        Thread oldThread = JsonConverter.InstanceHolder.instance.getInstance().convertFromJson(threadRequest, Thread.class);
        try {
            return ResponseEntity.ok(threadService.createThread(oldThread.getName()).toString());
        } catch (IOException | InterruptedException | CommonServiceException | URISyntaxException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/thread/{uuid}")
    public ResponseEntity<String> deleteThread(@PathVariable String uuid) {
        try {
            return ResponseEntity.ok(new JSONObject().put("deleted", threadService.removeThread(uuid)).toString());
        } catch (IOException | InterruptedException | CommonServiceException | URISyntaxException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
