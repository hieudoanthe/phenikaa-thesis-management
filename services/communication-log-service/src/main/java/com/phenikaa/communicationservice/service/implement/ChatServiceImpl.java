package com.phenikaa.communicationservice.service.implement;

import com.phenikaa.communicationservice.client.UserServiceClient;
import com.phenikaa.communicationservice.entity.ChatMessage;
import com.phenikaa.communicationservice.service.interfaces.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ReactiveMongoTemplate mongoTemplate;
    private final UserServiceClient userServiceClient;

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

    @Override
    public Flux<Map<String, Object>> getRecentMessages(String userId) {
        // Lấy 20 tin nhắn gần nhất của user
        return mongoTemplate.find(
            query(where("senderId").is(userId)
                .orOperator(where("receiverId").is(userId)))
                .with(Sort.by(Sort.Direction.DESC, "timestamp"))
                .limit(20),
            ChatMessage.class
        ).map(message -> {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", message.getId());
            messageMap.put("senderId", message.getSenderId());
            messageMap.put("receiverId", message.getReceiverId());
            messageMap.put("content", message.getContent());
            messageMap.put("timestamp", message.getTimestamp());
            return messageMap;
        });
    }

    @Override
    public Flux<String> getDistinctPartners(String userId) {
        // Lấy tất cả receiverIds mà user đã gửi và tất cả senderIds đã gửi cho user,
        // hợp nhất và loại bỏ trùng
        Flux<String> sentTo = mongoTemplate
            .query(ChatMessage.class)
            .distinct("receiverId")
            .matching(query(where("senderId").is(userId)))
            .as(String.class)
            .all();

        Flux<String> receivedFrom = mongoTemplate
            .query(ChatMessage.class)
            .distinct("senderId")
            .matching(query(where("receiverId").is(userId)))
            .as(String.class)
            .all();

        return Flux.merge(sentTo, receivedFrom)
            .filter(id -> id != null && !id.equals(userId))
            .distinct();
    }

}
