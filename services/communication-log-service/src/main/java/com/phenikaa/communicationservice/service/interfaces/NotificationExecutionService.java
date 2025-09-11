package com.phenikaa.communicationservice.service.interfaces;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public interface NotificationExecutionService {
    ExecutorService executor();
}
