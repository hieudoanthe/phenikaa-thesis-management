package com.phenikaa.communicationservice.service.interfaces;

import com.phenikaa.communicationservice.entity.ChatMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface ChatService {
    <T extends ChatMessage> Mono<T> saveMessage(T message);
    Flux<ChatMessage> getChatHistory(String user1, String user2);
    Flux<Map<String, Object>> getRecentMessages(String userId);
    Flux<String> getDistinctPartners(String userId);
}
