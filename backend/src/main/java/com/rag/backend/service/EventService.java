package com.rag.backend.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class EventService {

    private final Map<String, SseEmitter> emitterByUser = new ConcurrentHashMap<>();

    public void subscribe(String userId, SseEmitter emitter) {
        emitterByUser.put(userId, emitter);
    }

    public void unsubscribe(String userId) {
        emitterByUser.remove(userId);
    }

    public void notifyUser(String userId, String message) {
        SseEmitter emitter = emitterByUser.get(userId);
        if (emitter != null) {
            try {
                emitter.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
