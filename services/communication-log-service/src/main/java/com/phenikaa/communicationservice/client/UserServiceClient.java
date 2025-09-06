package com.phenikaa.communicationservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }

    public Mono<String> getUsernameById(Integer userId) {
        return webClient
                .get()
                .uri("/internal/users/get-username/{userId}", userId)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("");
    }

    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getUserById(String userId) {
        return webClient
                .get()
                .uri("/internal/users/{userId}", userId)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> {
                    Map<String, Object> result = new HashMap<>();
                    map.forEach((k, v) -> result.put(String.valueOf(k), v));
                    return result;
                })
                .onErrorReturn(Map.of());
    }

    @SuppressWarnings("unchecked")
    public Flux<Map<String, Object>> getUsersByIds(String... userIds) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/users/batch")
                        .queryParam("ids", String.join(",", userIds))
                        .build())
                .retrieve()
                .bodyToFlux(Map.class)
                .cast(Map.class)
                .map(map -> {
                    Map<String, Object> result = new HashMap<>();
                    map.forEach((k, v) -> result.put(String.valueOf(k), v));
                    return result;
                })
                .onErrorReturn(Map.of());
    }
}