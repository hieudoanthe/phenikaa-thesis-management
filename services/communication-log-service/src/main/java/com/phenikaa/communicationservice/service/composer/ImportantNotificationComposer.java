package com.phenikaa.communicationservice.service.composer;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;

public class ImportantNotificationComposer implements NotificationComposer {
    @Override
    public String compose(NotificationRequest req) {
        return "[IMPORTANT] " + req.getMessage();
    }
}