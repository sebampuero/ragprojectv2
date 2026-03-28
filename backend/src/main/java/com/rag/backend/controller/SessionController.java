package com.rag.backend.controller;

import com.rag.backend.dto.EventDTO;
import com.rag.backend.enums.EventType;
import com.rag.backend.service.SessionManagerService;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/session")
@Slf4j
public class SessionController {

    private final SessionManagerService sessionManagerService;

    public SessionController(SessionManagerService sessionManagerService) {
        this.sessionManagerService = sessionManagerService;
    }

    @GetMapping("/handshake")
    public EventDTO initialHandshake(@RequestParam String userId) {
        if (sessionManagerService.getActiveSession() != null
                && sessionManagerService.getActiveSession().getUserId().equals(userId)) {
            log.info("User {} is already in chatting session.", userId);
            return EventDTO.builder().event_name(EventType.CHATTING.name()).payload(null).build();
        } else if (sessionManagerService.isInQueue(userId)) {
            log.info("User {} is already in queue. Invalidating session.", userId);
            sessionManagerService.removeWaitingUser(userId);
        }
        String newUserId = UUID.randomUUID().toString();
        log.info("Generating new session userID: {}", newUserId);
        return EventDTO.builder().event_name(EventType.NEW.name()).payload(newUserId).build();
    }
}
