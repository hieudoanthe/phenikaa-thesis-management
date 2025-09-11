package com.phenikaa.communicationservice.broadcaster;

import com.phenikaa.communicationservice.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class NotificationPublisherProxy implements NotificationPublisher {

    private final NotificationBroadcaster delegate;

    private final Map<Integer, Long> lastPushAt = new ConcurrentHashMap<>();
    private static final long MIN_INTERVAL_MS = 50; // throttle nhẹ

    @Override
    public void publish(Integer receiverId, Notification notification) {
        if (receiverId == null || notification == null) return;

        long now = Instant.now().toEpochMilli();
        long last = lastPushAt.getOrDefault(receiverId, 0L);
        if (now - last < MIN_INTERVAL_MS) return; // chặn spam burst

        // có thể thêm kiểm tra quyền ở đây (ví dụ: white-list receiverId)
        delegate.publish(receiverId, notification);
        lastPushAt.put(receiverId, now);
    }

    @Override
    public Flux<Notification> subscribe(Integer receiverId) {
        // có thể thêm kiểm tra quyền/giới hạn subscriber
        return delegate.subscribe(receiverId);
    }
}