package com.phenikaa.communicationservice.service.interfaces;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public interface NotificationExecutionService {
    CompletableFuture<Void> runAsync(Runnable task);
    <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier);
    ExecutorService executor();
}
