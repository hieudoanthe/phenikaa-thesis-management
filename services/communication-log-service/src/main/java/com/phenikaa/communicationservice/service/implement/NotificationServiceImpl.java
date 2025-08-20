package com.phenikaa.communicationservice.service.implement;

import com.phenikaa.communicationservice.config.NotificationBroadcaster;
import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import com.phenikaa.communicationservice.entity.Notification;
import com.phenikaa.communicationservice.repository.NotificationRepository;
import com.phenikaa.communicationservice.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationBroadcaster notificationBroadcaster;

    @Override
    public Mono<Notification> saveAndPublish(NotificationRequest request) {
        Notification noti = Notification.builder()
                .studentId(request.getSenderId())
                .teacherId(request.getReceiverId())
                .message(request.getMessage())
                .read(false)
                .createdAt(System.currentTimeMillis())
                .build();

        return notificationRepository.save(noti)
                .doOnSuccess(notificationBroadcaster::publish);
    }

}
