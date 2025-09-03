package com.phenikaa.communicationservice.service.decorator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenikaa.communicationservice.client.UserServiceClient;
import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import jakarta.mail.internet.MimeMessage;
import com.phenikaa.communicationservice.service.NotificationExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class EmailNotificationDecorator extends BaseNotificationDecorator {

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
    private final UserServiceClient userServiceClient;
    private final NotificationExecutionService executionService;

    // Constructor cho Spring autowiring
    @Autowired
    public EmailNotificationDecorator(JavaMailSender mailSender, ObjectMapper objectMapper, UserServiceClient userServiceClient, NotificationExecutionService executionService) {
        super();
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
        this.userServiceClient = userServiceClient;
        this.executionService = executionService;
    }

    // Constructor cho decorator chain
    public EmailNotificationDecorator(NotificationDecorator wrapped, JavaMailSender mailSender, ObjectMapper objectMapper, UserServiceClient userServiceClient, NotificationExecutionService executionService) {
        super(wrapped);
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
        this.userServiceClient = userServiceClient;
        this.executionService = executionService;
    }

    @Override
    public void sendNotification(NotificationRequest request) {
        log.info("EmailNotificationDecorator.sendNotification called with type: {}", request.getType());
        
        // Chỉ gửi email cho các loại thông báo quan trọng
        if (shouldSendEmail(request.getType())) {
            log.info("Should send email for type: {}", request.getType());
            this.triggerEmailNotificationAsync(request);
        } else {
            log.info("Skipping email for type: {}", request.getType());
        }

        // Luôn gọi wrapped service (WebSocket)
        super.sendNotification(request);
    }

    @Override
    public void sendNotification(Map<String, Object> request) {
        log.info("EmailNotificationDecorator.sendNotification(Map) called with request: {}", request);
        
        // Convert Map to NotificationRequest
        try {
            NotificationRequest notificationRequest = objectMapper.convertValue(request, NotificationRequest.class);
            log.info("Converted to NotificationRequest with type: {}", notificationRequest.getType());
            
            // Chỉ gửi email cho các loại thông báo quan trọng
            if (shouldSendEmail(notificationRequest.getType())) {
                log.info("Should send email for type: {}", notificationRequest.getType());
                this.triggerEmailNotificationAsync(notificationRequest);
            } else {
                log.info("Skipping email for type: {}", notificationRequest.getType());
            }
        } catch (Exception e) {
            log.error("Error converting Map to NotificationRequest for email: {}", e.getMessage());
        }

        // Luôn gọi wrapped service
        super.sendNotification(request);
    }

    /**
     * Kiểm tra xem có nên gửi email cho loại thông báo này không
     */
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
               notificationType.equals("IMPORTANT");
               
        log.info("Should send email: {} for type: {}", shouldSend, notificationType);
        return shouldSend;
    }

    /**
     * Gửi email notification bất đồng bộ
     */
    public void triggerEmailNotificationAsync(NotificationRequest request) {
        log.info("Starting async email notification for receiver: {}", request.getReceiverId());

        CompletableFuture<Void> flow = userServiceClient.getUsernameById(request.getReceiverId())
                .toFuture()
                .exceptionally(throwable -> {
                    log.error("Error getting username from user-service: {}", throwable.getMessage());
                    return "";
                })
                .thenAcceptAsync(username -> {
                    if (username != null && !username.isEmpty()) {
                        log.info("Retrieved receiver email: {}", username);
                        sendEmailToUser(username, request);
                    } else {
                        log.warn("No email found for receiver ID: {}", request.getReceiverId());
                    }
                }, executionService.executor());

        flow.exceptionally(throwable -> {
            log.error("Error in email notification process: {}", throwable.getMessage());
            return null;
        });
    }

    /**
     * Gửi email đến user
     */
    private void sendEmailToUser(String receiverEmail, NotificationRequest request) {
        try {
            log.info("Creating email message for: {}", receiverEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(receiverEmail);
            helper.setSubject("Thông báo quan trọng - Hệ thống quản lý luận văn");
            helper.setText(buildEmailContent(request), true);

            log.info("Sending email to: {}", receiverEmail);
            mailSender.send(message);
            log.info("Email notification sent successfully to: {} for type: {}", receiverEmail, request.getType());

        } catch (Exception e) {
            log.error("Error sending email notification: {}", e.getMessage(), e);
        }
    }

    private String buildEmailContent(NotificationRequest request) {
        return String.format("""
            <html>
            <body>
                <h2>Thông báo quan trọng</h2>
                <p><strong>Nội dung:</strong> %s</p>
                <p><strong>Loại thông báo:</strong> %s</p>
                <p><strong>Thời gian:</strong> %s</p>
                <hr>
                <p><em>Hệ thống quản lý luận văn - Phenikaa University</em></p>
            </body>
            </html>
            """,
                request.getMessage(),
                request.getType(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
    }
}