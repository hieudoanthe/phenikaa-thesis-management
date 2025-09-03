package com.phenikaa.communicationservice.service.composer;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;

public interface NotificationComposer {
    String compose(NotificationRequest req);
}