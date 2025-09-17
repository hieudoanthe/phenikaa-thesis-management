package com.phenikaa.authservice.client;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.AuthenticatedUserResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Getter
@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${USER_SERVICE_URL:https://user-service-production-495b.up.railway.app}") String userServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(userServiceUrl).build();
    }

    public Mono<AuthenticatedUserResponse> verifyUser(LoginRequest request) {
        return webClient.post()
                .uri("/internal/users/verify")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthenticatedUserResponse.class);
    }

    public Mono<Void> saveRefreshToken(SaveRefreshTokenRequest request) {
        return webClient.post()
                .uri("/internal/users/save-refresh-token")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    public Mono<Void> deleteRefreshToken(String refreshToken) {
        return webClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/users/delete-refresh-token")
                        .queryParam("token", refreshToken)
                        .build())
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    public Mono<AuthenticatedUserResponse> getUserByRefreshToken(String refreshToken) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/users/get-userBy-refresh-token")
                        .queryParam("token", refreshToken)
                        .build())
                .retrieve()
                .bodyToMono(AuthenticatedUserResponse.class);
    }
}
