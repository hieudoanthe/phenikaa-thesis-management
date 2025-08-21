package com.phenikaa.communicationservice.config;

import com.phenikaa.communicationservice.entity.Notification;
import com.phenikaa.communicationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler implements WebSocketHandler {
    private final NotificationRepository notificationRepository;
    private final NotificationBroadcaster broadcaster;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        int receiverId = extractReceiverId(session);

        Flux<WebSocketMessage> oldNoti = notificationRepository
                .findByReceiverId(receiverId)
                .map(n -> formatNotification(session, n));

        Flux<WebSocketMessage> newNoti = broadcaster
                .subscribe(receiverId)
                .map(n -> formatNotification(session, n));

        Flux<WebSocketMessage> output = oldNoti.concatWith(newNoti);

        return session.send(output);
    }

    private int extractReceiverId(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        if (query != null && query.startsWith("receiverId=")) {
            try {
                return Integer.parseInt(query.split("=")[1]);
            } catch (NumberFormatException ignored) {}
        }
        throw new IllegalArgumentException("receiverId không hợp lệ hoặc thiếu!");
    }

    private WebSocketMessage formatNotification(WebSocketSession session, Notification n) {
        long createdAtMillis = n.getCreatedAt().toEpochMilli();
        String payload = String.format(
                "{\"id\":\"%s\",\"message\":\"%s\",\"senderId\":%d,\"receiverId\":%d,\"createdAt\":%d,\"read\":%b}",
                n.getId(),
                escapeJson(n.getMessage()),
                n.getSenderId(),
                n.getReceiverId(),
                createdAtMillis,
                n.isRead()
        );
        return session.textMessage(payload);
    }

    private String escapeJson(String str) {
        return str.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
