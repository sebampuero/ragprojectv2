package com.rag.backend.enums;

/**
 * Enumeration of event types sent to clients via SSE.
 */
public enum EventType {
    PROMOTED_CHAT,
    WAIT_IN_Q,
    DEMOTED,
    QUEUE_SIZE,
    ALREADY_REGISTERED
}
