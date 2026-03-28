package com.rag.backend.controller;

import com.rag.backend.service.SessionManagerService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class EventsController {

    private final SessionManagerService sessionManagerService;

    public EventsController(SessionManagerService sessionManagerService) {
        this.sessionManagerService = sessionManagerService;
    }

    @GetMapping("/events")
    public ResponseEntity<SseEmitter> subscribeToEvents(@RequestParam String userId) {
        if (userId.trim().isEmpty()) {
            log.warn("Empty userId provided for events subscription.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        SseEmitter newEmitter = new SseEmitter(0L);

        newEmitter.onCompletion(() -> {
            log.info("User {} unsubscribed, emitter completed", userId);
            sessionManagerService.removeWaitingUser(userId, newEmitter);
        });
        newEmitter.onTimeout(() -> {
            log.info("User {} unsubscribed, emitter timeout", userId);
            sessionManagerService.removeWaitingUser(userId, newEmitter);
        });
        newEmitter.onError((ex) -> {
            log.info("User {} unsubscribed, emitter error", userId);
            sessionManagerService.removeWaitingUser(userId, newEmitter);
        });

        if (sessionManagerService.getActiveSession() == null) {
            sessionManagerService.promoteUser(userId, newEmitter);
        } else {
            sessionManagerService.addToWaitingList(userId, newEmitter);
        }
        return ResponseEntity.ok(newEmitter);
    }
}
