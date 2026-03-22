package com.rag.backend.controller;

import com.rag.backend.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class EventsController {

    private final EventService eventService;

    public EventsController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/events")
    public ResponseEntity<SseEmitter> subscribeToEvents(@RequestParam String userId) {
        if (userId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        SseEmitter emitter = new SseEmitter(0L); // No timeout

        eventService.subscribe(userId, emitter);

        emitter.onCompletion(() -> eventService.unsubscribe(userId));
        emitter.onTimeout(() -> eventService.unsubscribe(userId));
        emitter.onError((ex) -> eventService.unsubscribe(userId));

        return ResponseEntity.ok(emitter);
    }
}
