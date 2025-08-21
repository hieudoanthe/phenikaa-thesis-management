package com.phenikaa.communicationservice.service.interfaces;

import com.phenikaa.communicationservice.entity.Notification;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Notification> createNotification(Integer senderId, Integer receiverId, String message);
    Mono<Long> markAllAsReadAndPublish(int receiverId);
    Mono<Notification> toggleReadAndPublish(int receiverId, String notificationId);
}
