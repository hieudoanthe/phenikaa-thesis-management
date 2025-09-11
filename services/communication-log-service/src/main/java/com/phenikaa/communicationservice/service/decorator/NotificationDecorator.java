package com.phenikaa.communicationservice.service.decorator;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;

public interface NotificationDecorator {
    void sendNotification(NotificationRequest request);
}