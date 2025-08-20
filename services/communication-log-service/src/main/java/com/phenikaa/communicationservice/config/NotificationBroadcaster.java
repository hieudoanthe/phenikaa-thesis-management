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

    public Flux<Notification> subscribe(int teacherId) {
        Sinks.Many<Notification> sink = sinks.computeIfAbsent(teacherId,
                id -> Sinks.many().multicast().onBackpressureBuffer());
        return sink.asFlux();
    }

    public void publish(Notification noti) {
        Sinks.Many<Notification> sink = sinks.get(noti.getTeacherId());
        if (sink != null) {
            sink.tryEmitNext(noti);
        }
    }
}
