package com.phenikaa.communicationservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {
    private final ReactiveRedisConnectionFactory factory;

    @Bean
    public ReactiveRedisMessageListenerContainer listenerContainer() {
        return new ReactiveRedisMessageListenerContainer(factory);
    }
}