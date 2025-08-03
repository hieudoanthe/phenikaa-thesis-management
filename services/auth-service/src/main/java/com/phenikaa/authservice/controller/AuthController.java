package com.phenikaa.authservice.controller;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.authservice.dto.response.AuthResponse;
import com.phenikaa.authservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

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
}
