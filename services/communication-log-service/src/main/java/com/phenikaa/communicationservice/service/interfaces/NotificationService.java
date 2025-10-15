package com.phenikaa.communicationservice.service.interfaces;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import com.phenikaa.communicationservice.entity.Notification;
import reactor.core.publisher.Mono;

public interface NotificationService {
    void sendNotification(NotificationRequest request);
    Mono<Notification> createNotification(Integer senderId, Integer receiverId, String message);
    Mono<Long> markAllAsReadAndPublish(int receiverId);
    Mono<Notification> toggleReadAndPublish(int receiverId, String notificationId);
    Mono<Notification> findById(String notificationId);
    Mono<Notification> markAsReadAndPublish(int receiverId, String notificationId);
}
