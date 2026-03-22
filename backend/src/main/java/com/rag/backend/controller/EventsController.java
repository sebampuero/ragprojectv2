package com.rag.backend.controller;

import com.rag.backend.enums.EventType;
import com.rag.backend.service.EventService;
import com.rag.backend.service.QueueService;
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

    private final EventService eventService;
    private final QueueService queueService;

    public EventsController(EventService eventService, QueueService queueService) {
        this.eventService = eventService;
        this.queueService = queueService;
    }

    @GetMapping("/events")
    public ResponseEntity<SseEmitter> subscribeToEvents(@RequestParam String userId) {
        if (userId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        SseEmitter emitter = new SseEmitter(0L); // No timeout

        eventService.subscribe(userId, emitter);

        if (queueService.getCurrentUserID() == null) {
            eventService.notifyUser(userId, EventType.PROMOTED_CHAT);
            queueService.tryConnect(userId);
            log.info("User {} was promoted to chat", userId);
        } else {
            eventService.notifyUser(userId, EventType.WAIT_IN_Q);
            queueService.tryConnect(userId);
            log.info("User {} is waiting in queue", userId);
        }

        emitter.onCompletion(() -> eventService.unsubscribe(userId));
        emitter.onTimeout(() -> eventService.unsubscribe(userId));
        emitter.onError((ex) -> eventService.unsubscribe(userId));

        return ResponseEntity.ok(emitter);
    }
}
