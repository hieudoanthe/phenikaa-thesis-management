package com.phenikaa.communicationservice.service.decorator;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public abstract class BaseNotificationDecorator implements NotificationDecorator {
    protected final NotificationDecorator wrapped;

    public BaseNotificationDecorator() {
        this.wrapped = null;
    }

    public BaseNotificationDecorator(NotificationDecorator wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void sendNotification(NotificationRequest request) {
        if (wrapped != null) {
            wrapped.sendNotification(request);
        }
    }

    @Override
    public void sendNotification(Map<String, Object> request) {
        if (wrapped != null) {
            wrapped.sendNotification(request);
        }
    }
}