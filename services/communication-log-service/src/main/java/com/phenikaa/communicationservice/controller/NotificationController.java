package com.phenikaa.communicationservice.controller;

import com.phenikaa.communicationservice.config.NotificationBroadcaster;
import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import com.phenikaa.communicationservice.entity.Notification;
import com.phenikaa.communicationservice.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public Mono<Notification> sendNotification(@RequestBody NotificationRequest request) {
        return notificationService.saveAndPublish(request);
    }
}
