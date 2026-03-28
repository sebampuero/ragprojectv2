package com.rag.backend.enums;

/**
 * Enumeration of event types sent to clients via SSE.
 */
public enum EventType {
    PROMOTED_CHAT,
    ALREADY_CONNECTED,
    WAIT_IN_Q,
    DEMOTED,
    QUEUE_SIZE,
    CHATTING,
    WAITING,
    NEW,
    PING
}
