package com.yk.web.controller;

import com.yk.common.service.CommonServiceException;
import com.yk.web.service.ThreadService;
import com.yk.web.vdm.Thread;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Controller
public class WebUIController {

    private final ThreadService threadService;

    public WebUIController(ThreadService threadService) {
        this.threadService = threadService;
    }

    @GetMapping("/thread/{uuid}")
    public String getThread(@NotNull Model model, @PathVariable String uuid) throws CommonServiceException, IOException, InterruptedException, URISyntaxException {
        Thread thread = threadService.readThread(uuid);
        model.addAttribute("thread", thread);
        model.addAttribute("threadItems", thread != null ? thread.getItems() : List.of());
        return "index";
    }

    @GetMapping({"/index", "/"})
    public String getIndex() throws IOException, InterruptedException, CommonServiceException, URISyntaxException {
        List<Thread> threads = threadService.getAllThreads();
        Thread thread = threads.size() > 0 ? threads.get(0) : null;
        return thread != null ? String.format("redirect:/thread/%s", thread.getID()) : "index";
    }


    @ExceptionHandler(Exception.class)
    public String handleException() {
        return "page-not-found";
    }

}
