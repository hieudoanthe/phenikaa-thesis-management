package com.phenikaa.communicationservice.service.implement;

import com.phenikaa.communicationservice.entity.ChatMessage;
import com.phenikaa.communicationservice.service.interfaces.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ReactiveMongoTemplate mongoTemplate;

    public <T extends ChatMessage> Mono<T> saveMessage(T message) {
        return mongoTemplate.save(message);
    }
    public Flux<ChatMessage> getChatHistory(String user1, String user2) {
        return mongoTemplate.find(
                query(where("senderId").in(user1, user2)
                        .and("receiverId").in(user1, user2))
                        .with(Sort.by(Sort.Direction.ASC, "timestamp")),
                ChatMessage.class
        );

    }

}
