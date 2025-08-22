package com.phenikaa.communicationservice.service.interfaces;

import com.phenikaa.communicationservice.entity.ChatMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatService {
    <T extends ChatMessage> Mono<T> saveMessage(T message);
    Flux<ChatMessage> getChatHistory(String user1, String user2);
}
