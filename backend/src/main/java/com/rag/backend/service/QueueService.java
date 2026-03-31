package com.rag.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
public class QueueService {
    private final Queue<String> userQueue = new ConcurrentLinkedQueue<>();

    public boolean joinQueue(String userId) {
        return userQueue.offer(userId);
    }

    public boolean removeFromQueue(String userId) {
        return userQueue.remove(userId);
    }

    public String getNextUserInQueue() {
        return userQueue.poll();
    }

    public String peekNextUserInQueue() {
        return userQueue.peek();
    }

    public int getQueuePosition(String userId) {
        return new ArrayList<>(userQueue).indexOf(userId) + 1;
    }

    public int getQueueSize() {
        return userQueue.size();
    }

    public boolean isUserInQueue(String userId) {
        return userQueue.contains(userId);
    }
}
