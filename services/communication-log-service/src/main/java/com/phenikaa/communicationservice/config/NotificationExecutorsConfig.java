package com.phenikaa.communicationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy;

@Configuration
public class NotificationExecutorsConfig {

    private ExecutorService executorService;

    @Bean(destroyMethod = "")
    public ExecutorService notificationExecutor() {
        ThreadFactory tf = r -> {
            Thread t = new Thread(r);
            t.setName("notification-exec");
            t.setDaemon(false);
            return t;
        };
        this.executorService = Executors.newFixedThreadPool(4, tf);
        return this.executorService;
    }

    @PreDestroy
    public void shutdownExecutor() {
        if (executorService == null) return;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }
}