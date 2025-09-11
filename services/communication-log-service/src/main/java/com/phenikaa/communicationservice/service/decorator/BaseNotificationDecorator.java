package com.phenikaa.communicationservice.service.decorator;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;

public abstract class BaseNotificationDecorator implements NotificationDecorator {
    protected final NotificationDecorator wrapped;

    protected BaseNotificationDecorator() {
        this.wrapped = null;
    }

    protected BaseNotificationDecorator(NotificationDecorator wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void sendNotification(NotificationRequest request) {
        if (wrapped != null) {
            wrapped.sendNotification(request);
        }
    }
}