package com.rag.backend.controller;

import com.rag.backend.enums.EventType;
import com.rag.backend.service.EventService;
import com.rag.backend.service.QueueService;
import com.rag.backend.service.ChatTimerService;
import com.rag.backend.dto.SseEventDTO;
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
    private final ChatTimerService chatTimerService;

    public EventsController(EventService eventService, QueueService queueService, ChatTimerService chatTimerService) {
        this.eventService = eventService;
        this.queueService = queueService;
        this.chatTimerService = chatTimerService;
    }

    @GetMapping("/events")
    public ResponseEntity<SseEmitter> subscribeToEvents(@RequestParam String userId) {
        if (userId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        SseEmitter existingEmitter = eventService.getEmitter(userId);
        if (existingEmitter != null) {
            log.info("User {} is already subscribed to events", userId);
            SseEmitter rejectEmitter = new SseEmitter(0L);
            try {
                rejectEmitter.send(SseEventDTO.builder()
                        .event_name(EventType.ALREADY_REGISTERED.name())
                        .payload(null)
                        .build());
                rejectEmitter.complete();
            } catch (Exception e) {
                log.error("Failed to send ALREADY_REGISTERED event", e);
            }
            return ResponseEntity.ok(rejectEmitter);
        }

        SseEmitter emitter = new SseEmitter(0L); // No timeout

        eventService.subscribe(userId, emitter);

        if (queueService.getCurrentUserID() == null) {
            queueService.setCurrentUserId(userId);
            eventService.notifyUser(userId, EventType.PROMOTED_CHAT);
            chatTimerService.startTimer(userId);
            log.info("User {} was promoted to chat", userId);
        } else {
            eventService.notifyUser(userId, EventType.WAIT_IN_Q);
            eventService.notifyAllUsers(EventType.QUEUE_SIZE, queueService.getQueueSize());
            queueService.joinQueue(userId);
            log.info("User {} is waiting in queue", userId);
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
