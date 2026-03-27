package com.rag.backend.controller;

import com.rag.backend.dto.EventDTO;
import com.rag.backend.enums.EventType;
import com.rag.backend.service.QueueService;
import com.rag.backend.service.SessionManagerService;
import com.rag.backend.service.SseEventService;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/queue")
public class QueueController {

    private final QueueService queueService;
    private final SseEventService eventService;
    private final SessionManagerService sessionManagerService;

    public QueueController(QueueService queueService, SseEventService eventService,
            SessionManagerService sessionManagerService) {
        this.queueService = queueService;
        this.eventService = eventService;
        this.sessionManagerService = sessionManagerService;
    }

    @GetMapping("/size")
    public int getQueueSize() {
        return queueService.getQueueSize();
    }

    @GetMapping("/handshake")
    public EventDTO initialHandshake(@RequestParam String userId) {
        if (sessionManagerService.getActiveSession() != null
                && sessionManagerService.getActiveSession().getUserId().equals(userId)) {
            return EventDTO.builder().event_name(EventType.CHATTING.name()).payload(null).build();
        }
        if (eventService.getEmitter(userId) != null) {
            return EventDTO.builder().event_name(EventType.WAITING.name()).payload(null).build();
        }
        return EventDTO.builder().event_name(EventType.NEW.name()).payload(UUID.randomUUID().toString()).build();
    }
}
