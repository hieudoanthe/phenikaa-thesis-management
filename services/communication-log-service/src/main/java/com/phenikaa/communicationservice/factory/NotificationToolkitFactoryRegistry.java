package com.phenikaa.communicationservice.factory;

import com.phenikaa.communicationservice.client.UserServiceClient;
import com.phenikaa.communicationservice.factory.implement.*;
import com.phenikaa.communicationservice.service.decorator.NotificationServiceDecorator;
import com.phenikaa.communicationservice.service.implement.NotificationExecutionServiceImpl;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationToolkitFactoryRegistry {
    private static volatile NotificationToolkitFactoryRegistry instance;
    private final Map<String, NotificationToolkitFactory> factories = new ConcurrentHashMap<>();
    private final NotificationServiceDecorator baseAdapter;
    private final JavaMailSender mailSender;
    private final UserServiceClient userServiceClient;
    private final NotificationExecutionServiceImpl executionService;

    // Private constructor
    private NotificationToolkitFactoryRegistry(NotificationServiceDecorator baseAdapter,
                                             JavaMailSender mailSender,
                                             UserServiceClient userServiceClient,
                                             NotificationExecutionServiceImpl executionService) {
        this.baseAdapter = baseAdapter;
        this.mailSender = mailSender;
        this.userServiceClient = userServiceClient;
        this.executionService = executionService;
        
        // Khởi tạo tất cả Singleton factories
        initializeFactories();
    }
    
    // Singleton getInstance method
    public static NotificationToolkitFactoryRegistry getInstance(NotificationServiceDecorator baseAdapter,
                                                                JavaMailSender mailSender,
                                                                UserServiceClient userServiceClient,
                                                                NotificationExecutionServiceImpl executionService) {
        if (instance == null) {
            synchronized (NotificationToolkitFactoryRegistry.class) {
                if (instance == null) {
                    instance = new NotificationToolkitFactoryRegistry(baseAdapter, mailSender, userServiceClient, executionService);
                }
            }
        }
        return instance;
    }
    
    private void initializeFactories() {
        AssignmentToolkitFactory assignmentFactory = AssignmentToolkitFactory.getInstance(baseAdapter, mailSender, userServiceClient, executionService);
        factories.put(assignmentFactory.supportsType(), assignmentFactory);
        
        ImportantToolkitFactory importantFactory = ImportantToolkitFactory.getInstance(baseAdapter, mailSender, userServiceClient, executionService);
        factories.put(importantFactory.supportsType(), importantFactory);
        
        DefenseScheduleToolkitFactory defenseFactory = DefenseScheduleToolkitFactory.getInstance(baseAdapter, mailSender, userServiceClient, executionService);
        factories.put(defenseFactory.supportsType(), defenseFactory);
        
        DefaultToolkitFactory defaultFactory = DefaultToolkitFactory.getInstance(baseAdapter, mailSender, userServiceClient, executionService);
        factories.put(defaultFactory.supportsType(), defaultFactory);
    }

    public NotificationToolkit resolve(String type) {
        if (type == null || type.trim().isEmpty()) {
            type = "DEFAULT";
        }

        NotificationToolkitFactory factory = factories.get(type);
        if (factory == null) {
            factory = factories.get("DEFAULT");
        }

        // Fallback cuối cùng nếu không có DEFAULT factory
        if (factory == null) {
            throw new IllegalStateException("No factory found for type: " + type + " and no DEFAULT factory available");
        }

        return new NotificationToolkit(factory.createComposer(), factory.createDecorator());
    }
}


