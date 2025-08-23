package com.phenikaa.communicationservice.config;

import com.phenikaa.communicationservice.entity.ChatMessage;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ChatBroadcaster {

    private final ReactiveRedisTemplate<String, ChatMessage> redisTemplate;

    public ChatBroadcaster(ReactiveRedisTemplate<String, ChatMessage> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Publish tin nhắn: gửi lên topic theo cặp user
    public Mono<Long> publish(ChatMessage msg) {
        String topic = getTopic(msg.getSenderId(), msg.getReceiverId());
        return redisTemplate.convertAndSend(topic, msg);
    }

    // Subscribe: nhận tất cả topic mà user tham gia
    public Flux<ChatMessage> subscribe(String userId) {
        return redisTemplate.listenToPattern("chat_*")
                .map(ReactiveSubscription.Message::getMessage)
                .filter(msg -> msg.getReceiverId().equals(userId));
    }

    // Sinh topic theo cặp user (thứ tự tăng dần để không bị lệch)
    private String getTopic(String user1, String user2) {
        return user1.compareTo(user2) < 0
                ? "chat_" + user1 + "_" + user2
                : "chat_" + user2 + "_" + user1;
    }
}
