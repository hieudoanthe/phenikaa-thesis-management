package com.phenikaa.communicationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenikaa.communicationservice.client.UserServiceClient;
import com.phenikaa.communicationservice.service.decorator.EmailNotificationDecorator;
import com.phenikaa.communicationservice.service.decorator.NotificationDecorator;
import com.phenikaa.communicationservice.service.decorator.NotificationServiceAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class NotificationDecoratorConfig {

    @Bean
    @Primary
    public NotificationDecorator notificationDecoratorChain(
            NotificationServiceAdapter notificationServiceAdapter,
            JavaMailSender mailSender,
            ObjectMapper objectMapper,
            UserServiceClient userServiceClient) {

        // Chain: Email -> WebSocket (base service)
        NotificationDecorator chain = notificationServiceAdapter;

        // Thêm Email decorator
        chain = new EmailNotificationDecorator(chain, mailSender, objectMapper, userServiceClient);

        return chain;
    }
}