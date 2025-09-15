package com.phenikaa.communicationservice.service.decorator;

import com.phenikaa.communicationservice.client.UserServiceClient;
import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import jakarta.mail.internet.MimeMessage;
import com.phenikaa.communicationservice.service.implement.NotificationExecutionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class EmailNotificationDecorator extends BaseNotificationDecorator {

    private final JavaMailSender mailSender;
    private final UserServiceClient userServiceClient;
    private final NotificationExecutionServiceImpl executionService;

    // Constructor cho Spring autowiring
    @Autowired
    public EmailNotificationDecorator(JavaMailSender mailSender, UserServiceClient userServiceClient, NotificationExecutionServiceImpl executionService) {
        super();
        this.mailSender = mailSender;
        this.userServiceClient = userServiceClient;
        this.executionService = executionService;
    }

    // Constructor cho decorator chain
    public EmailNotificationDecorator(NotificationDecorator wrapped, JavaMailSender mailSender, UserServiceClient userServiceClient, NotificationExecutionServiceImpl executionService) {
        super(wrapped);
        this.mailSender = mailSender;
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
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        if ("REGISTRATION_PERIOD".equalsIgnoreCase(request.getType())) {
            return String.format("""
                <html>
                <body style=\"font-family:Arial,Helvetica,sans-serif; color:#111827;\">
                    <div style=\"max-width:640px;margin:auto;border:1px solid #e5e7eb;border-radius:12px;overflow:hidden\">
                        <div style=\"background:#111827;color:#fff;padding:16px 20px\">
                            <h2 style=\"margin:0;font-size:18px\">Thông báo mở đợt đăng ký</h2>
                        </div>
                        <div style=\"padding:20px;background:#ffffff\">
                            <p style=\"margin:0 0 12px\">%s</p>
                            <p style=\"margin:0;color:#6b7280;font-size:12px\">Thời gian gửi: %s</p>
                        </div>
                        <div style=\"padding:14px 20px;background:#f9fafb;color:#6b7280;font-size:12px\">
                            Hệ thống quản lý luận văn - Phenikaa University
                        </div>
                    </div>
                </body>
                </html>
                """,
                    request.getMessage(), now);
        }

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
                now
        );
    }
}