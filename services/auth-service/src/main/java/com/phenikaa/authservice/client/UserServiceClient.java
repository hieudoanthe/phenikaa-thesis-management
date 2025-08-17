package com.phenikaa.authservice.client;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.AuthenticatedUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    private final String userServiceUrl = "http://localhost:8081";

    public Mono<AuthenticatedUserResponse> verifyUser(LoginRequest request) {
        return webClientBuilder.baseUrl(userServiceUrl)
                .build()
                .post()
                .uri("/internal/users/verify")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthenticatedUserResponse.class);
    }


    public Mono<Void> saveRefreshToken(SaveRefreshTokenRequest request) {
        return webClientBuilder.baseUrl(userServiceUrl)
                .build()
                .post()
                .uri("/internal/users/save-refresh-token")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    public Mono<Void> deleteRefreshToken(String refreshToken) {
        return webClientBuilder.baseUrl(userServiceUrl)
                .build()
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/users/delete-refresh-token")
                        .queryParam("token", refreshToken)
                        .build())
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    public Mono<AuthenticatedUserResponse> getUserByRefreshToken(String refreshToken) {
        return webClientBuilder.baseUrl(userServiceUrl)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/users/get-userBy-refresh-token")
                        .queryParam("token", refreshToken)
                        .build())
                .retrieve()
                .bodyToMono(AuthenticatedUserResponse.class);
    }

}
