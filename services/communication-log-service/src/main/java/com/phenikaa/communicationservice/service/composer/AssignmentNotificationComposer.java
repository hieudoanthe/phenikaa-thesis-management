package com.phenikaa.communicationservice.service.composer;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;

public class AssignmentNotificationComposer implements NotificationComposer {
    @Override
    public String compose(NotificationRequest req) {
        return "[ASSIGNMENT] " + req.getMessage();
    }
}