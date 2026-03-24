import { useState, useEffect } from 'react';

const JAVA_BACKEND_URL = 'http://localhost:8080';

const EventType = {
  PROMOTED_CHAT: 'PROMOTED_CHAT',
  WAIT_IN_Q: 'WAIT_IN_Q',
  DEMOTED: 'DEMOTED',
  QUEUE_SIZE: 'QUEUE_SIZE'
} as const;

export const useQueueEvents = () => {
  const [queueSize, setQueueSize] = useState<number>(0);
  const [isPromotedModalOpen, setIsPromotedModalOpen] = useState<boolean>(false);

  const onPromotedModalClose = () => {
    setIsPromotedModalOpen(false);
  };

  useEffect(() => {
    const userId = crypto.randomUUID();

    const eventSource = new EventSource(`${JAVA_BACKEND_URL}/events?userId=${userId}`);
    localStorage.setItem('userId', userId);

    eventSource.onmessage = (event) => {
      console.log('Received event:', event.data);
      try {
        const data = JSON.parse(event.data);
        if (data.event_name === EventType.PROMOTED_CHAT) {
          setIsPromotedModalOpen(true);
        } else if (data.event_name === EventType.QUEUE_SIZE) {
          setQueueSize(data.payload);
        }
      } catch (e) {
        console.error('Failed to parse SSE event:', e);
      }
    };

    eventSource.onerror = (error) => {
      console.error('EventSource failed:', error);
      eventSource.close();
    };

    return () => {
      eventSource.close();
    };
  }, []);

  return { queueSize, isPromotedModalOpen, onPromotedModalClose };
};
