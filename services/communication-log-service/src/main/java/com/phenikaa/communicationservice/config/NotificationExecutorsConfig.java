package com.phenikaa.communicationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class NotificationExecutorsConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService notificationExecutor() {
        ThreadFactory tf = r -> {
            Thread t = new Thread(r);
            t.setName("notification-exec");
            t.setDaemon(true);
            return t;
        };
        return Executors.newFixedThreadPool(4, tf);
    }
}