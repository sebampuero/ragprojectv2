export const EventType = {
    PROMOTED_CHAT: 'PROMOTED_CHAT',
    ALREADY_CONNECTED: 'ALREADY_CONNECTED',
    WAIT_IN_Q: 'WAIT_IN_Q',
    DEMOTED: 'DEMOTED',
    QUEUE_SIZE: 'QUEUE_SIZE',
    CHATTING: 'CHATTING',
    WAITING: 'WAITING',
    NEW: 'NEW',
} as const;

export type EventType = typeof EventType[keyof typeof EventType];
