package com.rag.backend.websocket;

import com.rag.backend.enums.EventType;
import com.rag.backend.models.ActiveSession;
import com.rag.backend.service.SseEventService;
import com.rag.backend.service.QueueService;
import com.rag.backend.service.SessionManagerService;

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

    private final SessionManagerService sessionManagerService;
    private final HttpClient httpClient;
    private final String ragchainUrl;

    public ChatWebSocketHandler(
            SessionManagerService sessionManagerService,
            @Value("${ragchain.url:http://localhost:8000}") String ragchainUrl) {
        this.sessionManagerService = sessionManagerService;
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
                ActiveSession activeSession = sessionManagerService.getActiveSession();
                if (activeSession != null && activeSession.getUserId().equals(userId)) {
                    session.getAttributes().put("userId", userId);
                    WebSocketSession oldSession = activeSession.getWebSocketSession();
                    if (oldSession != null) {
                        try {
                            oldSession.close();
                        } catch (Exception e) {
                            log.error("Failed to close old WebSocket session for user: {}", userId, e);
                        }
                    }
                    activeSession.setWebSocketSession(session);
                    log.info("User {} connected: {}", userId, session);
                } else {
                    session.close(CloseStatus.NOT_ACCEPTABLE.withReason("User is not the current user"));
                    log.info("User {} is not the current user", userId);
                }
            } else {
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

        if (sessionManagerService.getActiveSession() == null
                || !userId.equals(sessionManagerService.getActiveSession().getUserId())) {
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
            log.info("WebSocket connection closed for user {} with status {}", userId, status);
            sessionManagerService.demoteUser(userId);
        }
    }
}
