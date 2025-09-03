package com.phenikaa.communicationservice.service.decorator;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;

import java.util.Map;

public interface NotificationDecorator {
    void sendNotification(NotificationRequest request);
    void sendNotification(Map<String, Object> request);
}