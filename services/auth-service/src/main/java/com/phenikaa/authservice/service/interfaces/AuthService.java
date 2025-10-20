package com.phenikaa.authservice.service.interfaces;

import com.phenikaa.authservice.dto.response.AuthResponse;
import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.request.RefreshTokenRequest;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<AuthResponse> login(LoginRequest request);
    Mono<AuthResponse> refreshAccessToken(RefreshTokenRequest request);
}
