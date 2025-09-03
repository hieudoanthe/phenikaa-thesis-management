package com.phenikaa.communicationservice.service.decorator;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import com.phenikaa.communicationservice.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adapter class để wrap NotificationService thành NotificationDecorator
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceAdapter implements NotificationDecorator {
    
    private final NotificationService notificationService;

    @Override
    public void sendNotification(NotificationRequest request) {
        log.info("NotificationServiceAdapter.sendNotification called with request: senderId={}, receiverId={}, message={}, type={}", 
                request.getSenderId(), request.getReceiverId(), request.getMessage(), request.getType());
        
        // Convert NotificationRequest to NotificationService call
        notificationService.createNotification(
            request.getSenderId(),
            request.getReceiverId(),
            request.getMessage()
        ).subscribe(); // Fire and forget for reactive call
        
        log.info("NotificationServiceAdapter: WebSocket notification sent");
    }

    @Override
    public void sendNotification(Map<String, Object> request) {
        // Convert Map to NotificationRequest
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setSenderId((Integer) request.get("senderId"));
        notificationRequest.setReceiverId((Integer) request.get("receiverId"));
        notificationRequest.setMessage((String) request.get("message"));
        notificationRequest.setType((String) request.get("type"));
        
        sendNotification(notificationRequest);
    }
}
