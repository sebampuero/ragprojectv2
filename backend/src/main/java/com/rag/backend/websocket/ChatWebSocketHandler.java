package com.rag.backend.websocket;

import com.rag.backend.service.EventService;
import com.rag.backend.service.QueueService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final EventService eventService;
    private final QueueService queueService;

    public ChatWebSocketHandler(EventService eventService, QueueService queueService) {
        this.eventService = eventService;
        this.queueService = queueService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri != null) {
            String id = UriComponentsBuilder.fromUri(uri)
                    .build()
                    .getQueryParams()
                    .getFirst("id");

            if (id != null) {
                // Check if the id is the same the one currently being hold by the currentUser
                // in QueueService
                // if yes, then accept it?
                // if no, then reject it
            } else {
                // reject it
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // connect to streaming endpoint of the rag endpoint app and send messages back
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // when the connection is closed, set the next user in queue as current user
        // and notify them via SSE
    }
}
