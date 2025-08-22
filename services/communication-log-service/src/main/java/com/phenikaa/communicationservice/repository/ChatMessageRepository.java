package com.phenikaa.communicationservice.repository;

import com.phenikaa.communicationservice.entity.ChatMessage;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ChatMessageRepository extends ReactiveMongoRepository<ChatMessage, String> {
    Flux<ChatMessage> findBySenderIdAndReceiverId(String senderId, String receiverId);
}
