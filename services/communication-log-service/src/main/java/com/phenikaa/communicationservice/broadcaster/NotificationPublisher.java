package com.phenikaa.communicationservice.broadcaster;

import com.phenikaa.communicationservice.entity.Notification;
import reactor.core.publisher.Flux;

public interface NotificationPublisher {
    void publish(Integer receiverId, Notification notification);
    Flux<Notification> subscribe(Integer receiverId);
}