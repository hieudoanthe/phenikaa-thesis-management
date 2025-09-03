package com.phenikaa.communicationservice.service.composer.factory;

import com.phenikaa.communicationservice.service.composer.*;

public final class NotificationComposerFactory {
    private NotificationComposerFactory() {}

    public static NotificationComposer get(String type) {
        if (type == null) return new DefaultNotificationComposer();
        return switch (type) {
            case "ASSIGNMENT" -> new AssignmentNotificationComposer();
            case "DEFENSE_SCHEDULE" -> new DefenseScheduleNotificationComposer();
            case "IMPORTANT" -> new ImportantNotificationComposer();
            default -> new DefaultNotificationComposer();
        };
    }
}