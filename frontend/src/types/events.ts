export const EventType = {
    PROMOTED_CHAT: 'PROMOTED_CHAT',
    WAIT_IN_Q: 'WAIT_IN_Q',
    DEMOTED: 'DEMOTED',
    QUEUE_SIZE: 'QUEUE_SIZE',
    ALREADY_REGISTERED: 'ALREADY_REGISTERED'
} as const;

export type EventType = typeof EventType[keyof typeof EventType];
