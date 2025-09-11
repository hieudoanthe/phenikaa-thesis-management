package com.phenikaa.communicationservice.factory.implement;

import com.phenikaa.communicationservice.client.UserServiceClient;
import com.phenikaa.communicationservice.factory.NotificationToolkitFactory;
import com.phenikaa.communicationservice.service.composer.DefaultNotificationComposer;
import com.phenikaa.communicationservice.service.composer.NotificationComposer;
import com.phenikaa.communicationservice.service.decorator.EmailNotificationDecorator;
import com.phenikaa.communicationservice.service.decorator.NotificationDecorator;
import com.phenikaa.communicationservice.service.decorator.NotificationServiceDecorator;
import com.phenikaa.communicationservice.service.implement.NotificationExecutionServiceImpl;
import org.springframework.mail.javamail.JavaMailSender;

public class DefaultToolkitFactory implements NotificationToolkitFactory {
    private static volatile DefaultToolkitFactory instance;
    private final NotificationServiceDecorator baseAdapter;
    private final JavaMailSender mailSender;
    private final UserServiceClient userServiceClient;
    private final NotificationExecutionServiceImpl executionService;
    
    // Private constructor
    private DefaultToolkitFactory(NotificationServiceDecorator baseAdapter,
                                JavaMailSender mailSender,
                                UserServiceClient userServiceClient,
                                NotificationExecutionServiceImpl executionService) {
        this.baseAdapter = baseAdapter;
        this.mailSender = mailSender;
        this.userServiceClient = userServiceClient;
        this.executionService = executionService;
    }
    
    // Singleton getInstance method
    public static DefaultToolkitFactory getInstance(NotificationServiceDecorator baseAdapter,
                                                   JavaMailSender mailSender,
                                                   UserServiceClient userServiceClient,
                                                   NotificationExecutionServiceImpl executionService) {
        if (instance == null) {
            synchronized (DefaultToolkitFactory.class) {
                if (instance == null) {
                    instance = new DefaultToolkitFactory(baseAdapter, mailSender, userServiceClient, executionService);
                }
            }
        }
        return instance;
    }

    @Override
    public String supportsType() { return "DEFAULT"; }

    @Override
    public NotificationComposer createComposer() {
        return new DefaultNotificationComposer();
    }

    @Override
    public NotificationDecorator createDecorator() {
        return new EmailNotificationDecorator(baseAdapter, mailSender, userServiceClient, executionService);
    }
}


