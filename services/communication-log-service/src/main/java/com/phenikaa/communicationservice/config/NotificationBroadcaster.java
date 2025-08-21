package com.phenikaa.communicationservice.config;

import com.phenikaa.communicationservice.entity.Notification;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationBroadcaster {

    private final Map<Integer, Sinks.Many<Notification>> sinks = new ConcurrentHashMap<>();

    public void publish(Integer receiverId, Notification notification) {
        sinks.computeIfAbsent(receiverId,
                id -> Sinks.many().multicast().onBackpressureBuffer()
        ).tryEmitNext(notification);
    }

    public Flux<Notification> subscribe(Integer receiverId) {
        return sinks.computeIfAbsent(receiverId,
                id -> Sinks.many().multicast().onBackpressureBuffer()
        ).asFlux();
    }
}


