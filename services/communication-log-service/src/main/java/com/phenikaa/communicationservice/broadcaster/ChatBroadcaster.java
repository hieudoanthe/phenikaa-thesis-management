package com.phenikaa.communicationservice.broadcaster;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.phenikaa.communicationservice.entity.ChatMessage;
import com.phenikaa.communicationservice.entity.GroupMessage;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ChatBroadcaster {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public ChatBroadcaster(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Long> publish(ChatMessage msg) {
        String topic = userTopic(msg.getReceiverId());
        return redisTemplate.convertAndSend(topic, serialize(msg));
    }

    public Mono<Long> publishGroup(GroupMessage msg, java.util.List<String> memberIds) {
        String payload = serialize(msg);
        return Flux.fromIterable(memberIds)
                .flatMap(memberId -> redisTemplate.convertAndSend(userTopic(memberId), payload))
                .reduce(0L, Long::sum);
    }

    public Flux<String> subscribe(String userId) {
        String topic = userTopic(userId);
        return redisTemplate.listenToChannel(topic).map(m -> m.getMessage());
    }

    private String serialize(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "";
        }
    }

    private String userTopic(String userId) {
        return "chat_user_" + userId;
    }
}
