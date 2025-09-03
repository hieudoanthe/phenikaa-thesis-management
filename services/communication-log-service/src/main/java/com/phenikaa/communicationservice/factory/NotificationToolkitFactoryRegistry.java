package com.phenikaa.communicationservice.factory;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationToolkitFactoryRegistry {
    private final Map<String, NotificationToolkitFactory> factories = new ConcurrentHashMap<>();

    public NotificationToolkitFactoryRegistry(List<NotificationToolkitFactory> list) {
        list.forEach(f -> factories.put(f.supportsType(), f));
    }

    public NotificationToolkit resolve(String type) {
        // Xử lý trường hợp type null hoặc empty
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


