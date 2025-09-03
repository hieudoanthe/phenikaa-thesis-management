package com.phenikaa.communicationservice.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

@Service
public class NotificationExecutionService {

    private final ExecutorService notificationExecutor;

    public NotificationExecutionService(ExecutorService notificationExecutor) {
        this.notificationExecutor = notificationExecutor;
    }

    public CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, notificationExecutor);
    }

    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, notificationExecutor);
    }

    public ExecutorService executor() {
        return notificationExecutor;
    }
}


