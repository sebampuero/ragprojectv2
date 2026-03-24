package com.rag.backend.websocket;

import com.rag.backend.enums.EventType;
import com.rag.backend.service.EventService;
import com.rag.backend.service.QueueService;
import com.rag.backend.service.ChatTimerService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final EventService eventService;
    private final QueueService queueService;
    private final ChatTimerService chatTimerService;
    private final HttpClient httpClient;
    private final String ragchainUrl;

    public ChatWebSocketHandler(
            EventService eventService,
            QueueService queueService,
            ChatTimerService chatTimerService,
            @Value("${ragchain.url:http://localhost:8000}") String ragchainUrl) {
        this.eventService = eventService;
        this.queueService = queueService;
        this.chatTimerService = chatTimerService;
        this.ragchainUrl = ragchainUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri != null) {
            String userId = UriComponentsBuilder.fromUri(uri)
                    .build()
                    .getQueryParams()
                    .getFirst("userId");

            if (userId != null) {
                if (queueService.isUserConnected(userId)) {
                    session.getAttributes().put("userId", userId);
                    log.info("User connected: {}", userId);
                } else {
                    boolean removed = queueService.removeFromQueue(userId);
                    if (removed) {
                        log.info("User {} was removed from the queue", userId);
                    } else {
                        log.info("User {} was not in the queue", userId);
                    }
                    session.sendMessage(new TextMessage(EventType.DEMOTED.name()));
                    session.close(CloseStatus.NOT_ACCEPTABLE.withReason("User is not the current user"));
                }
            } else {
                session.sendMessage(new TextMessage(EventType.DEMOTED.name()));
                session.close(CloseStatus.BAD_DATA.withReason("Missing userId"));
                log.info("Missing userId for the Websocket connection");
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) {
            return;
        }

        if (!userId.equals(queueService.getCurrentUserID())) {
            session.sendMessage(new TextMessage(EventType.DEMOTED.name()));
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("User is not the current user"));
            return;
        }

        String userMessage = message.getPayload();
        String encodedMessage = URLEncoder.encode(userMessage, StandardCharsets.UTF_8);
        String url = ragchainUrl + "/chat/" + userId + "?user_input=" + encodedMessage;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<Stream<String>> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofLines());

            response.body().forEach(line -> {
                try {
                    session.sendMessage(new TextMessage(line));
                } catch (Exception e) {
                    log.error("Failed to send message to WebSocket client", e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to call RAG service", e);
            session.sendMessage(new TextMessage("Error: Could not reach RAG service, try again later."));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            log.info("WebSocket connection closed for user {}", userId);
            chatTimerService.cancelTimer();
            String promotedUserId = queueService.disconnect(userId);
            eventService.unsubscribe(userId);
            if (promotedUserId != null) {
                log.info("Promoting user {}", promotedUserId);
                eventService.notifyUser(promotedUserId, EventType.PROMOTED_CHAT);
                chatTimerService.startTimer(promotedUserId);
            }
        }
    }
}
