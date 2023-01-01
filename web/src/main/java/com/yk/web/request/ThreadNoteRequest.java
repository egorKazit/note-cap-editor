package com.yk.web.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ThreadNoteRequest {
    private final String name;
    private final String content;

}
