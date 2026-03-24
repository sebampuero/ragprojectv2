package com.rag.backend.service;

import com.rag.backend.enums.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ChatTimerService {

    private final EventService eventService;
    private final QueueService queueService;
    private final long timeoutSeconds;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> currentTimer;

    public ChatTimerService(EventService eventService, QueueService queueService,
            @Value("${chat.timeout.seconds}") long timeoutSeconds) {
        this.eventService = eventService;
        this.queueService = queueService;
        this.timeoutSeconds = timeoutSeconds;
    }

    public synchronized void startTimer(String userId) {
        cancelTimer();
        log.info("Starting chat timer for user {} for {} seconds", userId, timeoutSeconds);
        currentTimer = scheduler.schedule(() -> handleTimeout(userId), timeoutSeconds, TimeUnit.SECONDS);
    }

    public synchronized void cancelTimer() {
        if (currentTimer != null && !currentTimer.isDone()) {
            currentTimer.cancel(false);
        }
    }

    private void handleTimeout(String userId) {
        log.info("Chat timer expired for user {}", userId);
        if (userId.equals(queueService.getCurrentUserID())) {
            eventService.notifyUser(userId, EventType.DEMOTED);

            String nextUserId = queueService.disconnect(userId);
            eventService.notifyAllUsers(EventType.QUEUE_SIZE, queueService.getQueueSize());
            if (nextUserId != null) {
                log.info("Promoting user {} to chat after time expiration", nextUserId);
                eventService.notifyUser(nextUserId, EventType.PROMOTED_CHAT);
                startTimer(nextUserId);
            }
        }
    }
}
