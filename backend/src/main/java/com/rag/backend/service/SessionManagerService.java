package com.rag.backend.service;

import com.rag.backend.enums.EventType;
import com.rag.backend.models.ActiveSession;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class SessionManagerService {

    private final QueueService queueService;
    private final SseEventService sseEventService;
    private final AtomicReference<ActiveSession> activeSessionRef = new AtomicReference<>(null);
    private final long timeoutSeconds;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public SessionManagerService(QueueService queueService, SseEventService sseEventService,
            @Value("${chat.timeout.seconds}") long timeoutSeconds) {
        this.queueService = queueService;
        this.sseEventService = sseEventService;
        this.timeoutSeconds = timeoutSeconds;
    }

    public ActiveSession getActiveSession() {
        return activeSessionRef.get();
    }

    public void removeWaitingUser(String userId, SseEmitter emitter) {
        if (queueService.isUserInQueue(userId)) {
            sseEventService.notifyAllUsers(EventType.QUEUE_SIZE, queueService.getQueueSize());
            queueService.removeFromQueue(userId);
            sseEventService.unsubscribe(userId, emitter);
        }
    }

    public void removeWaitingUser(String userId) {
        SseEmitter emitter = sseEventService.getEmitter(userId);
        removeWaitingUser(userId, emitter);
    }

    public void promoteNextUserInQueue() {
        if (activeSessionRef.get() != null) {
            return;
        }

        String nextUserId = queueService.getNextUserInQueue();
        sseEventService.notifyAllUsers(EventType.QUEUE_SIZE, queueService.getQueueSize());
        if (nextUserId != null) {
            SseEmitter emitter = sseEventService.getEmitter(nextUserId);

            if (emitter != null) {
                try {
                    sseEventService.throwyNotifyUser(nextUserId, EventType.PING, null);
                    promoteUser(nextUserId, emitter);
                } catch (Exception e) {
                    log.info("The emitter for user {} is dead, promoting next user in queue.", nextUserId);
                    promoteNextUserInQueue();
                }
            } else {
                // lazy eviction, one user disconnected while waiting and may have been left in
                // the queue
                // but the emitter existed. If the emitter is null, it means the user left the
                // waiting queue
                // and we won't promote ghost users
                log.info("User {} disconnected while waiting. Skipping to next.", nextUserId);
                promoteNextUserInQueue();
            }
        }
    }

    public void promoteUser(String userId, SseEmitter emitter) {
        if (sseEventService.getEmitter(userId) == null) {
            sseEventService.subscribe(userId, emitter);
        }
        Runnable expirationTask = () -> {
            log.info("Session timeout reached for user: {}", userId);
            demoteUser(userId);
            promoteNextUserInQueue();
        };

        ScheduledFuture<?> future = scheduler.schedule(
                expirationTask,
                timeoutSeconds,
                TimeUnit.SECONDS);

        ActiveSession newSession = new ActiveSession(userId, future);

        if (activeSessionRef.compareAndSet(null, newSession)) {
            log.info("User {} successfully promoted to active session.", userId);
            sseEventService.notifyUser(userId, EventType.PROMOTED_CHAT);
            // promoted user does not need events anymore
            sseEventService.unsubscribe(userId, emitter);
        } else {
            future.cancel(false);
            log.warn("Failed to promote user {}: Another session is currently active.", userId);
            queueService.joinQueue(userId);
            sseEventService.notifyUser(userId, EventType.WAIT_IN_Q);
        }
    }

    public void demoteUser(String userId) {
        sseEventService.notifyUser(userId, EventType.DEMOTED);
        SseEmitter emitter = sseEventService.getEmitter(userId);
        if (emitter != null) {
            sseEventService.unsubscribe(userId, emitter);
        }

        ActiveSession current = activeSessionRef.get();
        if (current != null && current.getUserId().equals(userId)) {
            current.cancelTimer();
            try {
                if (current.getWebSocketSession() != null) {
                    current.getWebSocketSession().close();
                }
            } catch (IOException e) {
                log.error("Failed to close WebSocket session for user: {}", userId, e);
            }
            activeSessionRef.compareAndSet(current, null);
        }
    }

    public void addToWaitingList(String userId, SseEmitter emitter) {
        if (!queueService.isUserInQueue(userId)) {
            queueService.joinQueue(userId);
        }
        sseEventService.subscribe(userId, emitter);
        sseEventService.notifyUser(userId, EventType.WAIT_IN_Q);
        sseEventService.notifyAllUsers(EventType.QUEUE_SIZE, queueService.getQueueSize());
    }

    public boolean isInQueue(String userId) {
        return queueService.isUserInQueue(userId);
    }
}
