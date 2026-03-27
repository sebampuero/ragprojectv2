package com.rag.backend.controller;

import com.rag.backend.enums.EventType;
import com.rag.backend.service.SseEventService;
import com.rag.backend.service.QueueService;
import com.rag.backend.service.SessionManagerService;
import com.rag.backend.dto.EventDTO;

import java.io.IOException;

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

    private final SseEventService eventService;
    private final SessionManagerService sessionManagerService;

    public EventsController(SseEventService eventService, SessionManagerService sessionManagerService) {
        this.eventService = eventService;
        this.sessionManagerService = sessionManagerService;
    }

    @GetMapping("/events")
    public ResponseEntity<SseEmitter> subscribeToEvents(@RequestParam String userId) {
        if (userId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (eventService.getEmitter(userId) != null) {
            eventService.notifyUser(userId, EventType.WAITING);
            // return a new emitter that sends a single message ALREADY_CONNECTED and then
            // closes, this would happen in a new tab
            SseEmitter emitter = new SseEmitter(0L);
            try {
                emitter.send(EventType.ALREADY_CONNECTED);
            } catch (IOException e) {
                log.error("Failed to send ALREADY_CONNECTED event to user {}", userId, e);
            }
            emitter.complete();
            return ResponseEntity.ok(emitter);
        }

        SseEmitter emitter = new SseEmitter(0L);

        if (sessionManagerService.getActiveSession() == null) {
            sessionManagerService.promoteUser(userId, emitter);
        } else {
            sessionManagerService.addToWaitingList(userId, emitter);
        }

        emitter.onCompletion(() -> {
            log.info("User {} unsubscribed, emitter completed", userId);
            eventService.unsubscribe(userId);
        });
        emitter.onTimeout(() -> {
            log.info("User {} unsubscribed, emitter timeout", userId);
            eventService.unsubscribe(userId);
        });
        emitter.onError((ex) -> {
            log.info("User {} unsubscribed, emitter error, {} ", userId, ex);
            eventService.unsubscribe(userId);
        });

        return ResponseEntity.ok(emitter);
    }
}
