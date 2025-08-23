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
        String userId = session.getHandshakeInfo().getUri().getQuery()
                .replace("userId=", "");

        // Nhận tin từ client
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
                .flatMap(msg -> chatService.saveMessage(msg)
                        .then(broadcaster.publish(msg))
                        .thenReturn(msg))
                .doOnError(Throwable::printStackTrace);

        // Gửi tin đến client
        Flux<WebSocketMessage> output = broadcaster.subscribe(userId)
                .map(msg -> {
                    try {
                        return session.textMessage(mapper.writeValueAsString(msg));
                    } catch (Exception e) {
                        return session.textMessage("");
                    }
                })
                .doOnError(Throwable::printStackTrace);

        return session.send(output)
                .and(input.then());
    }
}
