package com.phenikaa.communicationservice.controller;

import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import com.phenikaa.communicationservice.entity.Notification;
import com.phenikaa.communicationservice.factory.NotificationToolkitFactoryRegistry;
import com.phenikaa.communicationservice.service.decorator.NotificationDecorator;
import com.phenikaa.communicationservice.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationDecorator notificationDecorator;
    private final NotificationToolkitFactoryRegistry toolkitRegistry;

    @PostMapping("/send")
    public ResponseEntity<String> send(@RequestBody NotificationRequest req) {
        log.info("NotificationController.send called with request: {}", req);
//        public Mono<Notification> send(@RequestBody NotificationRequest req) {
//            return notificationService.createNotification(
//                    req.getSenderId(),
//                    req.getReceiverId(),
//                    req.getMessage()
//            );
        try {
//            // Sử dụng NotificationDecorator để gửi thông báo (bao gồm email + WebSocket)
//            notificationDecorator.sendNotification(req);
//
//            log.info("Notification sent successfully via decorator");

            // Abstract Factory: tạo bộ công cụ theo type nếu có, fallback decorator chain mặc định
            var toolkit = toolkitRegistry.resolve(req.getType());
            if (toolkit != null && toolkit.decoratorChain() != null) {
                // dùng chain theo type
                toolkit.decoratorChain().sendNotification(req);
            } else {
                // fallback chain mặc định cấu hình sẵn
                notificationDecorator.sendNotification(req);
            }

            log.info("Notification sent successfully via decorator/toolkit");
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
}