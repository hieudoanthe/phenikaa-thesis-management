package com.phenikaa.communicationservice.factory.implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenikaa.communicationservice.client.UserServiceClient;
import com.phenikaa.communicationservice.factory.NotificationToolkitFactory;
import com.phenikaa.communicationservice.service.composer.DefenseScheduleNotificationComposer;
import com.phenikaa.communicationservice.service.decorator.EmailNotificationDecorator;
import com.phenikaa.communicationservice.service.decorator.NotificationDecorator;
import com.phenikaa.communicationservice.service.decorator.NotificationServiceAdapter;
import com.phenikaa.communicationservice.service.implement.NotificationExecutionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefenseScheduleToolkitFactory implements NotificationToolkitFactory {
    private final NotificationServiceAdapter baseAdapter;
    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
    private final UserServiceClient userServiceClient;
    private final NotificationExecutionServiceImpl executionService;

    @Override
    public String supportsType() { return "DEFENSE_SCHEDULE"; }

    @Override
    public DefenseScheduleNotificationComposer createComposer() {
        return new DefenseScheduleNotificationComposer();
    }

    @Override
    public NotificationDecorator createDecorator() {
        // Defense schedule luôn gửi email
        return new EmailNotificationDecorator(baseAdapter, mailSender, objectMapper, userServiceClient, executionService);
    }
}
