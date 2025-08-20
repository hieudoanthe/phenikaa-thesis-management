package com.phenikaa.communicationservice.service.interfaces;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import com.phenikaa.communicationservice.entity.Notification;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Notification> saveAndPublish(NotificationRequest request);
}
