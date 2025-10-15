package com.phenikaa.communicationservice.controller;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import com.phenikaa.communicationservice.entity.Notification;
import com.phenikaa.communicationservice.repository.NotificationRepository;
import com.phenikaa.communicationservice.service.decorator.EmailDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/notifications")
@Slf4j
public class NotificationController {

    private final EmailDecorator notificationService;
    private final NotificationRepository notificationRepository;

    public NotificationController(EmailDecorator notificationService, NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @PostMapping("/send")
    public ResponseEntity<String> send(@RequestBody NotificationRequest req) {
        log.info("NotificationController.send called with request: {}", req);
        try {
            // Sử dụng email decorator - logic phân biệt type đã có trong decorator
            notificationService.sendNotification(req);

            log.info("Notification sent successfully via decorator");
            return ResponseEntity.ok("Notification sent successfully");

        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error sending notification: " + e.getMessage());
        }
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

    @PutMapping("/{notificationId}/mark-read")
    public Mono<ResponseEntity<Notification>> markAsRead(@PathVariable String notificationId) {
        // Tìm notification trước để lấy receiverId
        return notificationRepository.findById(notificationId)
                .flatMap(notification -> 
                    notificationService.markAsReadAndPublish(notification.getReceiverId(), notificationId)
                        .map(ResponseEntity::ok)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}