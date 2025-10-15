package com.phenikaa.communicationservice.service.decorator;

import com.phenikaa.communicationservice.broadcaster.NotificationPublisher;
import com.phenikaa.communicationservice.client.UserServiceClient;
import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import com.phenikaa.communicationservice.entity.Notification;
import com.phenikaa.communicationservice.repository.NotificationRepository;
import com.phenikaa.communicationservice.service.implement.NotificationServiceImpl;
import com.phenikaa.communicationservice.service.interfaces.NotificationService;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class EmailDecorator implements NotificationService {

    private final NotificationService wrappedService;
    private final JavaMailSender mailSender;
    private final UserServiceClient userServiceClient;

    @Autowired
    public EmailDecorator(NotificationRepository notificationRepository,
                          NotificationPublisher notificationBroadcaster,
                          ReactiveMongoTemplate mongoTemplate,
                          JavaMailSender mailSender,
                          UserServiceClient userServiceClient) {
        this.wrappedService = new NotificationServiceImpl(notificationRepository, notificationBroadcaster, mongoTemplate);
        this.mailSender = mailSender;
        this.userServiceClient = userServiceClient;
    }

    @Override
    public void sendNotification(NotificationRequest request) {
        log.info("EmailDecorator.sendNotification called with type: {}", request.getType());
        
        // Chỉ gửi email cho các loại thông báo quan trọng
        if (shouldSendEmail(request.getType())) {
            log.info("Should send email for type: {}", request.getType());
            this.triggerEmailNotificationAsync(request);
        } else {
            log.info("Skipping email for type: {}", request.getType());
        }

        // Luôn gọi wrapped service (WebSocket)
        wrappedService.sendNotification(request);
    }

    @Override
    public Mono<Notification> createNotification(Integer senderId, Integer receiverId, String message) {
        return wrappedService.createNotification(senderId, receiverId, message);
    }

    @Override
    public Mono<Long> markAllAsReadAndPublish(int receiverId) {
        return wrappedService.markAllAsReadAndPublish(receiverId);
    }

    @Override
    public Mono<Notification> toggleReadAndPublish(int receiverId, String notificationId) {
        return wrappedService.toggleReadAndPublish(receiverId, notificationId);
    }

    @Override
    public Mono<Notification> findById(String notificationId) {
        return wrappedService.findById(notificationId);
    }

    @Override
    public Mono<Notification> markAsReadAndPublish(int receiverId, String notificationId) {
        return wrappedService.markAsReadAndPublish(receiverId, notificationId);
    }

    private boolean shouldSendEmail(String notificationType) {
        log.info("Checking if should send email for type: {}", notificationType);
        
        if (notificationType == null) {
            log.info("Notification type is null, skipping email");
            return false;
        }
        
        // Chỉ gửi email cho các thông báo quan trọng
        boolean shouldSend = notificationType.equals("ASSIGNMENT") || 
               notificationType.equals("UNASSIGNMENT") ||
               notificationType.equals("DEFENSE_SCHEDULE") ||
               notificationType.equals("IMPORTANT") ||
               notificationType.equals("REGISTRATION_PERIOD");
               
        log.info("Should send email: {} for type: {}", shouldSend, notificationType);
        return shouldSend;
    }

    private void triggerEmailNotificationAsync(NotificationRequest request) {
        CompletableFuture.runAsync(() -> {
            try {
                String receiverEmail = userServiceClient.getUsernameById(request.getReceiverId()).block();
                if (receiverEmail == null || receiverEmail.trim().isEmpty()) {
                    log.warn("No email found for receiver ID: {}", request.getReceiverId());
                    return;
                }

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(receiverEmail);
                helper.setSubject("Thông báo từ Hệ thống quản lý luận văn Phenikaa University");
                helper.setText(buildEmailContent(request), true);

                log.info("Sending email to: {}", receiverEmail);
                mailSender.send(message);
                log.info("Email notification sent successfully to: {} for type: {}", receiverEmail, request.getType());

            } catch (Exception e) {
                log.error("Error sending email notification: {}", e.getMessage(), e);
            }
        });
    }

    private String buildEmailContent(NotificationRequest request) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        if ("REGISTRATION_PERIOD".equalsIgnoreCase(request.getType())) {
            return request.getMessage();
        }

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9;">
                    <h2 style="color: #0056b3; text-align: center;">THÔNG BÁO TỪ HỆ THỐNG QUẢN LÝ LUẬN VĂN</h2>
                    <p>Kính gửi Quý Thầy/Cô,</p>
                    <div style="background-color: #e9f7ff; border-left: 5px solid #007bff; padding: 15px; margin: 20px 0; border-radius: 4px;">
                        <p style="margin: 0;">%s</p>
                    </div>
                    <p>Loại thông báo: <strong>%s</strong></p>
                    <p>Trân trọng,</p>
                    <p><strong>Hệ thống quản lý luận văn Phenikaa University</strong></p>
                    <hr style="border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 0.8em; color: #777; text-align: center;">Email được gửi tự động vào lúc %s.</p>
                </div>
            </body>
            </html>
            """,
                request.getMessage(),
                request.getType(),
                now
        );
    }
}
