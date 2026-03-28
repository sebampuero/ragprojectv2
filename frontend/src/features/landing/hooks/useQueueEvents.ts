import { useState, useEffect, useRef } from 'react';
import { EventType } from '../../../types/events';
import { useNavigate } from 'react-router-dom';

const JAVA_BACKEND_URL = import.meta.env.VITE_JAVA_BACKEND_HTTP;


export const useQueueEvents = () => {
  const [queueSize, setQueueSize] = useState<number>(0);
  const [isPromotedModalOpen, setIsPromotedModalOpen] = useState<boolean>(false);
  const navigate = useNavigate();
  const eventSourceRef = useRef<EventSource | null>(null);

  const onPromotedModalClose = () => {
    setIsPromotedModalOpen(false);
  };

  useEffect(() => {
    let userId = localStorage.getItem('userId');

    const initialHandshake = async () => {
      try {
        const response = await fetch(`${JAVA_BACKEND_URL}/session/handshake?userId=${userId}`);
        if (response.ok) {
          const data = await response.json();
          console.log('Received handshake response:', data);
          if (data.event_name === EventType.CHATTING) {
            navigate('/chat');
            // this would open a new ws session and revoke the old one if the user is still active
            return;
          } else if (data.event_name === EventType.NEW) {
            localStorage.setItem('userId', data.payload);
            userId = data.payload;
          }
        }

        eventSourceRef.current = new EventSource(`${JAVA_BACKEND_URL}/events?userId=${userId}`);

        eventSourceRef.current.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            console.log('Received event:', data);
            if (data.event_name === EventType.PROMOTED_CHAT) {
              setIsPromotedModalOpen(true);
            } else if (data.event_name === EventType.QUEUE_SIZE) {
              setQueueSize(data.payload);
            }
          } catch (e) {
            console.error('Failed to parse SSE event:', e);
          }
        };

        eventSourceRef.current.onerror = (error) => {
          console.error('EventSource failed:', error);
          eventSourceRef.current?.close();
        };
      } catch (error) {
        console.error('Failed to fetch queue size:', error);
      }
    };

    initialHandshake();

    return () => {
      console.log("Unmounting useQueueEvents")
      eventSourceRef.current?.close();
    };
  }, []);

  return { queueSize, isPromotedModalOpen, onPromotedModalClose };
};
