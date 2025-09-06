package com.phenikaa.communicationservice.service.implement;

import com.phenikaa.communicationservice.client.UserServiceClient;
import com.phenikaa.communicationservice.dto.response.ConversationResponse;
import com.phenikaa.communicationservice.service.interfaces.ConversationService;
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

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ReactiveMongoTemplate mongoTemplate;
    private final UserServiceClient userServiceClient;

    @Override
    public Flux<ConversationResponse> getUserConversations(String userId) {
        return getUserConversationsWithDetails(userId);
    }

    @Override
    public Flux<ConversationResponse> getUserConversationsWithDetails(String userId) {
        // Tạo aggregation pipeline để lấy danh sách conversations của user
        MatchOperation matchOperation = Aggregation.match(
            new Criteria().orOperator(
                where("senderId").is(userId),
                where("receiverId").is(userId)
            )
        );

        // Group theo conversation partner và lấy tin nhắn cuối cùng
        GroupOperation groupOperation = Aggregation.group("senderId", "receiverId")
            .last("content").as("lastMessage")
            .last("timestamp").as("lastMessageTime")
            .count().as("messageCount");

        // Project để tạo conversation object
        ProjectionOperation projectionOperation = Aggregation.project()
            .andExpression("_id.senderId").as("senderId")
            .andExpression("_id.receiverId").as("receiverId")
            .and("lastMessage").as("lastMessage")
            .and("lastMessageTime").as("lastMessageTime")
            .and("messageCount").as("messageCount");

        // Sort theo thời gian tin nhắn cuối cùng
        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.DESC, "lastMessageTime"));

        Aggregation aggregation = Aggregation.newAggregation(
            matchOperation,
            groupOperation,
            projectionOperation,
            sortOperation
        );

        return mongoTemplate.aggregate(aggregation, "chat_messages", Map.class)
            .collectList()
            .flatMapMany(conversations -> {
                // Lấy tất cả partner IDs
                var partnerIds = conversations.stream()
                    .map(conv -> {
                        String senderId = (String) conv.get("senderId");
                        String receiverId = (String) conv.get("receiverId");
                        return senderId.equals(userId) ? receiverId : senderId;
                    })
                    .distinct()
                    .toList();

                if (partnerIds.isEmpty()) {
                    return Flux.empty();
                }

                // Lấy thông tin user cho tất cả partners
                return userServiceClient.getUsersByIds(partnerIds.toArray(new String[0]))
                    .collectMap(user -> (String) user.get("userId"))
                    .flatMapMany(userMap -> 
                        Flux.fromIterable(conversations)
                            .map(conv -> {
                                String senderId = (String) conv.get("senderId");
                                String receiverId = (String) conv.get("receiverId");
                                String partnerId = senderId.equals(userId) ? receiverId : senderId;
                                
                                Map<String, Object> userInfo = userMap.getOrDefault(partnerId, Map.of());
                                
                                return ConversationResponse.builder()
                                    .partnerId(partnerId)
                                    .partnerName((String) userInfo.getOrDefault("fullName", "Unknown User"))
                                    .partnerEmail((String) userInfo.getOrDefault("email", ""))
                                    .partnerAvatar((String) userInfo.getOrDefault("avt", ""))
                                    .lastMessage((String) conv.get("lastMessage"))
                                    .lastMessageTime((Instant) conv.get("lastMessageTime"))
                                    .messageCount(((Number) conv.get("messageCount")).longValue())
                                    .unreadCount(0L)
                                    .conversationId(partnerId)
                                    .build();
                            })
                    );
            });
    }
}
