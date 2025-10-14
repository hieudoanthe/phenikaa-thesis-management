package com.phenikaa.authservice.client;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.AuthenticatedUserResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

@Getter
@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${USER_SERVICE_HOST}") String userServiceUrl) {
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

    // Password reset methods
    public Mono<Map<String, Object>> createPasswordResetToken(String username) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/users/forgot-password")
                        .queryParam("username", username)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> validateResetToken(String token) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/users/validate-reset-token")
                        .queryParam("token", token)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> resetPassword(String token, String newPassword) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/users/reset-password")
                        .queryParam("token", token)
                        .queryParam("newPassword", newPassword)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> changePassword(Integer userId, String currentPassword, String newPassword) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("userId", userId);
        body.put("currentPassword", currentPassword);
        body.put("newPassword", newPassword);

        return webClient.put()
                .uri("/internal/users/change-password")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
