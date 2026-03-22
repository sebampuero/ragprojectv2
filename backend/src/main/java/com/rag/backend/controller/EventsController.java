package com.rag.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class EventsController {

    @GetMapping("/events")
    public SseEmitter subscribeToEvents() {
        // Configuration for SSE endpoint
        // Logic to be added
        return null;
    }
}
