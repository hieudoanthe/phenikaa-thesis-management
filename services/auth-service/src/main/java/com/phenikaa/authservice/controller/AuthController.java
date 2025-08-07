package com.phenikaa.authservice.controller;

import com.phenikaa.authservice.client.UserServiceClient;
import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.authservice.dto.response.AuthResponse;
import com.phenikaa.authservice.service.implement.AuthService;
import com.phenikaa.dto.response.RefreshTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserServiceClient userServiceClient;

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody LoginRequest request) {
        return authService.login(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    System.err.println("Login failed: " + e.getMessage());
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.status(401).build());
                });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@RequestBody RefreshTokenResponse request) {
        return userServiceClient.deleteRefreshToken(request.getRefreshToken())
                .thenReturn(ResponseEntity.ok().<Void>build());
    }

}
