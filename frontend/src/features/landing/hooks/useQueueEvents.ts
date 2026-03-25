import { useState, useEffect } from 'react';
import { EventType } from '../../../types/events';

const JAVA_BACKEND_URL = import.meta.env.VITE_JAVA_BACKEND_HTTP;


export const useQueueEvents = () => {
  const [queueSize, setQueueSize] = useState<number>(0);
  const [isPromotedModalOpen, setIsPromotedModalOpen] = useState<boolean>(false);

  const onPromotedModalClose = () => {
    setIsPromotedModalOpen(false);
  };

  useEffect(() => {

    let userId = localStorage.getItem('userId');
    if (!userId) {
      userId = crypto.randomUUID();
      localStorage.setItem('userId', userId);
      console.log('New userId created:', userId);
    }

    const fetchQueueSize = async () => {
      try {
        const response = await fetch(`${JAVA_BACKEND_URL}/queue/size`);
        if (response.ok) {
          const size = await response.json();
          setQueueSize(size);
        }
      } catch (error) {
        console.error('Failed to fetch queue size:', error);
      }
    };
    fetchQueueSize();

    const eventSource = new EventSource(`${JAVA_BACKEND_URL}/events?userId=${userId}`);

    eventSource.onmessage = (event) => {
      console.log('Received event:', event.data);
      try {
        const data = JSON.parse(event.data);
        if (data.event_name === EventType.PROMOTED_CHAT) {
          setIsPromotedModalOpen(true);
        } else if (data.event_name === EventType.QUEUE_SIZE) {
          setQueueSize(data.payload);
        } else if (data.event_name === EventType.WAIT_IN_Q) {
          setQueueSize(data.payload);
        } else if (data.event_name === EventType.ALREADY_REGISTERED) {
          alert('You are already registered in the queue or chatting!');
          eventSource.close();
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
