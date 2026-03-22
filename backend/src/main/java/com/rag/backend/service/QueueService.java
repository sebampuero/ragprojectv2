package com.rag.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class QueueService {
    private final Queue<String> userQueue = new ConcurrentLinkedQueue<>();
    private final AtomicReference<String> currentUser = new AtomicReference<>();

    public boolean tryConnect(String userId) {
        if (currentUser.get() == null) {
            return currentUser.compareAndSet(null, userId);
        }
        userQueue.offer(userId);
        return false;
    }

    public String disconnect() {
        return setNextUserInQueueAsCurrent();
    }

    private String setNextUserInQueueAsCurrent() {
        String nextUser = userQueue.poll();
        if (nextUser != null) {
            log.info("Setting {} as current user", nextUser);
            currentUser.set(nextUser);
        } else {
            currentUser.set(null);
        }
        return nextUser;
    }

    public boolean removeFromQueue(String userId) {
        return userQueue.remove(userId);
    }

    public String getCurrentUserID() {
        return currentUser.get();
    }

    public int getQueuePosition(String userId) {
        if (userId.equals(currentUser.get())) {
            return 0;
        }
        return new ArrayList<>(userQueue).indexOf(userId) + 1;
    }

    public boolean isUserConnected(String userId) {
        return userId.equals(currentUser.get());
    }
}
