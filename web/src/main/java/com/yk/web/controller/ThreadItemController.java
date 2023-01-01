package com.yk.web.controller;

import com.yk.common.service.CommonServiceException;
import com.yk.web.request.ThreadNoteRequest;
import com.yk.web.service.ThreadItemService;
import com.yk.web.vdm.ThreadItem;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

@Controller
@Log4j2
public class ThreadItemController {

    private final ThreadItemService threadItemService;

    public ThreadItemController(ThreadItemService threadItemService) {
        this.threadItemService = threadItemService;
    }

    @GetMapping("/thread/item/{uuid}")
    public ResponseEntity<String> getItem(@PathVariable String uuid) throws CommonServiceException {
        try {
            return ResponseEntity.ok(threadItemService.readItem(uuid).toString());
        } catch (IOException | InterruptedException | URISyntaxException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/thread/{threadUuid}/attachment")
    public ResponseEntity<String> uploadFile(@PathVariable String threadUuid, @RequestParam("source") String sourceName, @RequestParam("file") MultipartFile file) {
        ThreadItem threadItem;
        try {
            threadItem = threadItemService.createAttachment(threadUuid, sourceName, file.getInputStream());
        } catch (InterruptedException | IOException | CommonServiceException | URISyntaxException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok(threadItem.toString());
    }

    @GetMapping("/thread/attachment/{uuid}")
    public void downloadFile(@PathVariable String uuid, @NotNull HttpServletResponse response) {
        try (InputStream inputStream = threadItemService.readAttachment(uuid)) {
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException | CommonServiceException | InterruptedException | URISyntaxException exception) {
            log.error("Error on attachment download: {}", uuid, exception);
        }
    }

    @DeleteMapping("/thread/attachment/{uuid}")
    public ResponseEntity<String> deleteFile(@PathVariable String uuid) {
        try {
            return ResponseEntity.ok(new JSONObject().put("deleted", threadItemService.deleteAttachment(uuid)).toString());
        } catch (CommonServiceException | IOException | InterruptedException | URISyntaxException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/thread/{threadUuid}/note")
    public ResponseEntity<String> createNote(@PathVariable String threadUuid, @RequestBody @NotNull ThreadNoteRequest threadNoteRequest) {
        ThreadItem threadItem = null;
        try {
            threadItem = threadItemService.createNote(threadUuid, threadNoteRequest.getContent());
        } catch (CommonServiceException | InterruptedException | IOException | URISyntaxException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok(threadItem.toString());
    }

    @PutMapping("/thread/note/{uuid}")
    public ResponseEntity<String> updateNote(@PathVariable String uuid, @RequestBody @NotNull ThreadNoteRequest threadNoteRequest)
            throws CommonServiceException {
        ThreadItem threadItem = null;
        try {
            threadItem = threadItemService.updateNote(uuid, threadNoteRequest.getContent());
        } catch (IOException | InterruptedException | URISyntaxException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok(threadItem.toString());
    }

    @DeleteMapping("/thread/note/{uuid}")
    public ResponseEntity<String> deleteNote(@PathVariable String uuid) {
        try {
            return ResponseEntity.ok(new JSONObject().put("deleted", threadItemService.deleteNote(uuid)).toString());
        } catch (CommonServiceException | IOException | InterruptedException | URISyntaxException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
