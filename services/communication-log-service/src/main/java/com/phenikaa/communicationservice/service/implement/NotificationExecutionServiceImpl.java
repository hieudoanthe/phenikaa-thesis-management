package com.phenikaa.communicationservice.service.implement;

import com.phenikaa.communicationservice.service.interfaces.NotificationExecutionService;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

@Service
public class NotificationExecutionServiceImpl implements NotificationExecutionService {

    private final ExecutorService notificationExecutor;

    public NotificationExecutionServiceImpl(ExecutorService notificationExecutor) {
        this.notificationExecutor = notificationExecutor;
    }

    public ExecutorService executor() {
        return notificationExecutor;
    }
}


