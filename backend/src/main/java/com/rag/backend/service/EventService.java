package com.rag.backend.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rag.backend.enums.EventType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
public class EventService {

    private final Map<String, SseEmitter> emitterByUser = new ConcurrentHashMap<>();

    public void subscribe(String userId, SseEmitter emitter) {
        log.info("User subscribed: {}", userId);
        emitterByUser.put(userId, emitter);
    }

    public void unsubscribe(String userId) {
        log.info("User unsubscribed: {}", userId);
        emitterByUser.remove(userId);
    }

    public void notifyUser(String userId, EventType eventType) {
        log.info("Notifying user: {} with event: {}", userId, eventType);
        SseEmitter emitter = emitterByUser.get(userId);
        if (emitter != null) {
            try {
                emitter.send(eventType.name());
            } catch (Exception e) {
                log.error("Failed to notify user: {}", userId, e);
            }
        }
    }

}
