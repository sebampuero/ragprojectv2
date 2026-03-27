package com.rag.backend.models;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ScheduledFuture;

@Slf4j
@Getter
@Setter
public class ActiveSession {

    private final String userId;
    private ScheduledFuture<?> scheduledFuture;
    private WebSocketSession webSocketSession;

    public ActiveSession(String userId, ScheduledFuture<?> scheduledFuture) {
        this.userId = userId;
        this.scheduledFuture = scheduledFuture;
    }

    public void cancelTimer() {
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(false);
            log.info("Cancelled chat timer for user {}", userId);
        }
    }

}
