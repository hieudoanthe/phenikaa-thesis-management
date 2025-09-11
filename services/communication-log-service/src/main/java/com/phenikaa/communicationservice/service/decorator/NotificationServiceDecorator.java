package com.phenikaa.communicationservice.service.decorator;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import com.phenikaa.communicationservice.service.composer.NotificationComposer;
import com.phenikaa.communicationservice.service.composer.factory.NotificationComposerFactory;
import com.phenikaa.communicationservice.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceDecorator implements NotificationDecorator {

    private final NotificationService notificationService;

    @Override
    public void sendNotification(NotificationRequest request) {
        log.info("NotificationServiceDecorator.sendNotification called with request: senderId={}, receiverId={}, message={}, type={}",
                request.getSenderId(), request.getReceiverId(), request.getMessage(), request.getType());

        NotificationComposer composer = NotificationComposerFactory.get(request.getType());
        String composedMessage = composer.compose(request);

        notificationService.createNotification(
                request.getSenderId(),
                request.getReceiverId(),
                composedMessage
        ).subscribe();

        log.info("NotificationServiceDecorator: notification persisted & broadcasted");
    }
}