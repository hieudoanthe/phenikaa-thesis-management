package com.phenikaa.communicationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.phenikaa.communicationservice.entity.ChatMessage;
import com.phenikaa.communicationservice.service.interfaces.ChatService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ChatBroadcaster broadcaster;
    private final ChatService chatService;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public ChatWebSocketHandler(ChatBroadcaster broadcaster, ChatService chatService) {
        this.broadcaster = broadcaster;
        this.chatService = chatService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        URI uri = session.getHandshakeInfo().getUri();
        String query = uri.getQuery();
        String userId = query != null && query.startsWith("userId=")
                ? query.substring("userId=".length())
                : "unknown";

        // Input: nhận tin nhắn từ client → lưu + publish
        Flux<ChatMessage> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .filter(payload -> payload != null && !payload.trim().isEmpty())
                .map(payload -> {
                    try {
                        return mapper.readValue(payload, ChatMessage.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(chatMsg ->
                        chatService.saveMessage(chatMsg)
                                .then(broadcaster.publish(chatMsg))
                                .thenReturn(chatMsg)
                )
                .doOnError(Throwable::printStackTrace);

        // Output: tin nhắn từ Redis
        Flux<WebSocketMessage> output = broadcaster.subscribe(userId)
                .map(chatMsg -> {
                    try {
                        return session.textMessage(mapper.writeValueAsString(chatMsg));
                    } catch (Exception e) {
                        return session.textMessage("");
                    }
                })
                .doOnError(Throwable::printStackTrace);

        // Kết hợp: output gửi về client, input chạy nền (không làm session complete)
        return session.send(output)
                .and(input.then()); // input chạy liên tục, không ép đóng
    }


}

