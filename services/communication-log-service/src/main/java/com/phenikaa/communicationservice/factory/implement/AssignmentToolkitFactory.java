package com.phenikaa.communicationservice.factory.implement;

import com.phenikaa.communicationservice.client.UserServiceClient;
import com.phenikaa.communicationservice.factory.NotificationToolkitFactory;
import com.phenikaa.communicationservice.service.composer.AssignmentNotificationComposer;
import com.phenikaa.communicationservice.service.composer.NotificationComposer;
import com.phenikaa.communicationservice.service.decorator.EmailNotificationDecorator;
import com.phenikaa.communicationservice.service.decorator.NotificationDecorator;
import com.phenikaa.communicationservice.service.decorator.NotificationServiceDecorator;
import com.phenikaa.communicationservice.service.implement.NotificationExecutionServiceImpl;
import org.springframework.mail.javamail.JavaMailSender;

public class AssignmentToolkitFactory implements NotificationToolkitFactory {
    private static volatile AssignmentToolkitFactory instance;
    private final NotificationServiceDecorator baseAdapter;
    private final JavaMailSender mailSender;
    private final UserServiceClient userServiceClient;
    private final NotificationExecutionServiceImpl executionService;
    
    // Private constructor
    private AssignmentToolkitFactory(NotificationServiceDecorator baseAdapter,
                                   JavaMailSender mailSender,
                                   UserServiceClient userServiceClient,
                                   NotificationExecutionServiceImpl executionService) {
        this.baseAdapter = baseAdapter;
        this.mailSender = mailSender;
        this.userServiceClient = userServiceClient;
        this.executionService = executionService;
    }
    
    // Singleton getInstance method
    public static AssignmentToolkitFactory getInstance(NotificationServiceDecorator baseAdapter,
                                                      JavaMailSender mailSender,
                                                      UserServiceClient userServiceClient,
                                                      NotificationExecutionServiceImpl executionService) {
        if (instance == null) {
            synchronized (AssignmentToolkitFactory.class) {
                if (instance == null) {
                    instance = new AssignmentToolkitFactory(baseAdapter, mailSender, userServiceClient, executionService);
                }
            }
        }
        return instance;
    }

    @Override
    public String supportsType() { return "ASSIGNMENT"; }

    @Override
    public NotificationComposer createComposer() {
        return new AssignmentNotificationComposer();
    }

    @Override
    public NotificationDecorator createDecorator() {
        return new EmailNotificationDecorator(baseAdapter, mailSender, userServiceClient, executionService);
    }
}
