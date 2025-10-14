package com.phenikaa.authservice.client;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Getter
@Component
public class CommunicationServiceClient {

    private final WebClient webClient;

    public CommunicationServiceClient(WebClient.Builder webClientBuilder,
                                     @Value("${COMMUNICATION_SERVICE_HOST}") String communicationServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(communicationServiceUrl).build();
    }

    public Mono<Map<String, Object>> sendPasswordResetEmail(String email, String token, String resetUrl) {
        Map<String, Object> requestBody = Map.of(
            "email", email,
            "token", token,
            "resetUrl", resetUrl
        );

        return webClient.post()
                .uri("/api/communication-service/admin/send-password-reset-email")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> sendNotification(Map<String, Object> payload) {
        return webClient.post()
                .uri("/notifications/send")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
