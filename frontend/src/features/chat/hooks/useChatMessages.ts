import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { EventType } from '../../../types/events';

export interface ChatMessage {
    content: string;
    isUser: boolean;
}


export const useChatMessages = () => {
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [isConnected, setIsConnected] = useState(false);
    const [isProcessingMessage, setIsProcessingMessage] = useState(false);
    const wsRef = useRef<WebSocket | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        const userId = localStorage.getItem('userId');
        const WS_URL = import.meta.env.VITE_JAVA_BACKEND_WS;
        if (!userId) {
            console.error('No userId found in localStorage');
            navigate('/');
            return;
        }

        const wsUrl = `${WS_URL}/websocket?userId=${userId}`;
        const ws = new WebSocket(wsUrl);
        wsRef.current = ws;

        ws.onopen = () => {
            setIsConnected(true);
            console.log('WebSocket connected');
        };

        ws.onmessage = (event) => {
            setIsProcessingMessage(false);

            const chunk = event.data;

            if (chunk === EventType.DEMOTED) {
                alert("Your session expired!")
                localStorage.removeItem('userId');
                navigate('/');
                return;
            }

            const data = JSON.parse(chunk);

            setMessages((prevMessages) => {
                const newMessages = [...prevMessages];
                const lastMessage = newMessages[newMessages.length - 1];

                if (lastMessage && !lastMessage.isUser) {
                    lastMessage.content += data.content;
                } else {
                    newMessages.push({
                        content: data.content,
                        isUser: false,
                    });
                }

                return newMessages;
            });
        };

        ws.onerror = (error) => {
            console.error('WebSocket error:', error);
            localStorage.removeItem('userId');
            navigate('/');
        };

        ws.onclose = (event) => {
            setIsConnected(false);
            console.log('WebSocket disconnected');
            if (event.code === 4001) {
                alert("Your chat was resumed in another tab.");
                return;
            }
            localStorage.removeItem('userId');
            navigate('/'); // TODO: the socket may close for many reasons, 
            // and there should be a reconnect logic.
            // will be handled later
        };

        return () => {
            if (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING) {
                ws.close();
            }
            wsRef.current = null;
        };
    }, []);

    const sendMessage = useCallback((text: string) => {
        if (!text.trim()) return;

        if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
            const userMessage: ChatMessage = {
                content: text,
                isUser: true,
            };

            setMessages((prev) => [...prev, userMessage]);
            setIsProcessingMessage(true);

            wsRef.current.send(text);
        } else {
            console.warn('WebSocket is not connected');
        }
    }, []);

    return {
        messages,
        isConnected,
        isProcessingMessage,
        sendMessage,
    };
};
