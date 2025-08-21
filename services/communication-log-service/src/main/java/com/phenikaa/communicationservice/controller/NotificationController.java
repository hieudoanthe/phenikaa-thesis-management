package com.phenikaa.communicationservice.controller;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import com.phenikaa.communicationservice.entity.Notification;
import com.phenikaa.communicationservice.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public Mono<Notification> send(@RequestBody NotificationRequest req) {
        return notificationService.createNotification(
                req.getSenderId(),
                req.getReceiverId(),
                req.getMessage()
        );
    }

    @PutMapping("/mark-all-read/{receiverId}")
    public Mono<ResponseEntity<String>> markAllAsRead(@PathVariable int receiverId) {
        return notificationService.markAllAsReadAndPublish(receiverId)
                .map(count -> ResponseEntity.ok("Updated " + count + " notifications"))
                .defaultIfEmpty(ResponseEntity.ok("No notifications to update"));
    }

    @PatchMapping("/{receiverId}/{notificationId}/toggle")
    public Mono<ResponseEntity<Notification>> toggleRead(
            @PathVariable int receiverId,
            @PathVariable String notificationId) {

        return notificationService.toggleReadAndPublish(receiverId, notificationId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
