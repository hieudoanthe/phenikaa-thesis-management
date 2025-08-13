package com.phenikaa.authservice.client;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    private final String userServiceUrl = "http://localhost:8081";

    public Mono<UserInfoResponse> verifyUser(LoginRequest request) {
        return webClientBuilder.baseUrl(userServiceUrl)
                .build()
                .post()
                .uri("/internal/users/verify")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserInfoResponse.class);
    }


    public Mono<Void> saveRefreshToken(SaveRefreshTokenRequest request) {
        return webClientBuilder.baseUrl(userServiceUrl)
                .build()
                .post()
                .uri("/internal/users/saveRefreshToken")
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
                        .path("/internal/users/deleteRefreshToken")
                        .queryParam("token", refreshToken)
                        .build())
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    public Mono<UserInfoResponse> getUserByRefreshToken(String refreshToken) {
        return webClientBuilder.baseUrl(userServiceUrl)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/users/getUserByRefreshToken")
                        .queryParam("token", refreshToken)
                        .build())
                .retrieve()
                .bodyToMono(UserInfoResponse.class);
    }

}
