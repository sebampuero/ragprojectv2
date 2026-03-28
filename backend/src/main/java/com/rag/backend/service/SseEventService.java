package com.rag.backend.service;

import java.io.IOError;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rag.backend.enums.EventType;
import com.rag.backend.dto.EventDTO;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
public class SseEventService {

    private final Map<String, SseEmitter> emitterByUser = new ConcurrentHashMap<>();

    public void subscribe(String userId, SseEmitter emitter) {
        emitterByUser.put(userId, emitter);
    }

    public void unsubscribe(String userId, SseEmitter emitterToRemove) {
        emitterByUser.remove(userId, emitterToRemove);
    }

    public SseEmitter getEmitter(String userId) {
        return emitterByUser.get(userId);
    }

    public void notifyUser(String userId, EventType eventType) {
        notifyUser(userId, eventType, null);
    }

    public void notifyAllUsers(EventType eventType, Object payload) {
        for (String userId : emitterByUser.keySet()) {
            notifyUser(userId, eventType, payload);
        }
    }

    public void notifyUser(String userId, EventType eventType, Object payload) {
        SseEmitter emitter = emitterByUser.get(userId);
        if (emitter != null) {
            try {
                EventDTO event = EventDTO.builder()
                        .event_name(eventType.name())
                        .payload(payload)
                        .build();
                emitter.send(event);
            } catch (Exception e) {
                log.error("Failed to notify user: {}", userId, e);
            }
        }
    }

    public void throwyNotifyUser(String userId, EventType eventType, Object payload) throws IOException {
        SseEmitter emitter = emitterByUser.get(userId);
        EventDTO event = EventDTO.builder()
                .event_name(eventType.name())
                .payload(payload)
                .build();
        emitter.send(event);
    }

}
