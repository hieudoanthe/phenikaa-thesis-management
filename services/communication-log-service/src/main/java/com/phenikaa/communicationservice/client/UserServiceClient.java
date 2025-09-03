package com.phenikaa.communicationservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
}