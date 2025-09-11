package com.phenikaa.communicationservice.broadcaster;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenikaa.communicationservice.entity.Notification;
import com.phenikaa.communicationservice.util.JsonMapperProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class NotificationBroadcaster {
    private final Map<Integer, Sinks.Many<Notification>> sinks = new ConcurrentHashMap<>();
    private final ReactiveStringRedisTemplate redis;
    private final ReactiveRedisMessageListenerContainer container;
    private final ObjectMapper mapper = JsonMapperProvider.getInstance().getMapper();

    @PostConstruct
    public void subscribeAll() {
        // Pattern subscribe tất cả kênh notifications:*
        container.receive(PatternTopic.of("notifications:*"))
                .flatMap(msg -> {
                    try {
                        String channel = msg.getChannel();
                        int receiverId = Integer.parseInt(channel.substring("notifications:".length()));
                        Notification noti = mapper.readValue(msg.getMessage(), Notification.class);
                        return sinks
                                .computeIfAbsent(receiverId, id -> Sinks.many().multicast().onBackpressureBuffer())
                                .tryEmitNext(noti).isSuccess() ? Mono.empty() : Mono.empty();
                    } catch (Exception e) {
                        return Mono.empty();
                    }
                })
                .subscribe();
    }

    public void publish(Integer receiverId, Notification notification) {
        // 1) Đẩy ra local sinks (cho WS trong instance)
        sinks.computeIfAbsent(receiverId, id -> Sinks.many().multicast().onBackpressureBuffer())
                .tryEmitNext(notification);
        // 2) Phát tán qua Redis cho các instance khác
        try {
            String json = mapper.writeValueAsString(notification);
            redis.convertAndSend("notifications:" + receiverId, json).subscribe();
        } catch (Exception ignored) {}
    }

    public Flux<Notification> subscribe(Integer receiverId) {
        return sinks.computeIfAbsent(receiverId, id -> Sinks.many().multicast().onBackpressureBuffer()).asFlux();
    }
}