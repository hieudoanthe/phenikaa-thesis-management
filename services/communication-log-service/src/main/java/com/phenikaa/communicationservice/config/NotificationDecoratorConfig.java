package com.phenikaa.communicationservice.config;

import com.phenikaa.communicationservice.client.UserServiceClient;
import com.phenikaa.communicationservice.service.decorator.EmailNotificationDecorator;
import com.phenikaa.communicationservice.service.decorator.NotificationServiceDecorator;
import com.phenikaa.communicationservice.service.implement.NotificationExecutionServiceImpl;
import com.phenikaa.communicationservice.service.decorator.NotificationDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class NotificationDecoratorConfig {

    @Bean
    @Primary
    public NotificationDecorator notificationDecoratorChain(
            NotificationServiceDecorator notificationServiceDecorator,
            JavaMailSender mailSender,
            UserServiceClient userServiceClient,
            NotificationExecutionServiceImpl executionService) {

        // Chain: Email -> WebSocket (base service)
        NotificationDecorator chain = notificationServiceDecorator;

        // Thêm Email decorator
        chain = new EmailNotificationDecorator(chain, mailSender, userServiceClient, executionService);

        return chain;
    }
}