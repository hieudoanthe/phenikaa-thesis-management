package com.phenikaa.communicationservice.config;

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

    private final NotificationBroadcaster broadcaster;
    private final NotificationRepository notificationRepository;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // Lấy teacherId từ query param
        int teacherId = 0;
        String query = session.getHandshakeInfo().getUri().getQuery();
        if (query != null && query.startsWith("teacherId=")) {
            try {
                teacherId = Integer.parseInt(query.split("=")[1]);
                System.out.println("teacherId: " + teacherId);
            } catch (NumberFormatException e) {
                System.out.println("teacherId không hợp lệ trong query param!");
            }
        } else {
            System.out.println("teacherId không được cung cấp!");
        }

        // Notification cũ chưa đọc
        Flux<WebSocketMessage> oldNoti = notificationRepository.findByTeacherIdAndReadFalse(teacherId)
                .map(noti -> session.textMessage(noti.getMessage()));

        // Notification mới realtime
        Flux<WebSocketMessage> newNoti = broadcaster.subscribe(teacherId)
                .map(noti -> session.textMessage(noti.getMessage()));

        return session.send(Flux.merge(oldNoti, newNoti))
                .and(session.receive().then());
    }
}
