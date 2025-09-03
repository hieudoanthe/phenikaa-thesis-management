package com.phenikaa.communicationservice.service.composer;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;

public class DefenseScheduleNotificationComposer implements NotificationComposer {
    @Override
    public String compose(NotificationRequest req) {
        return "[DEFENSE] " + req.getMessage();
    }
}