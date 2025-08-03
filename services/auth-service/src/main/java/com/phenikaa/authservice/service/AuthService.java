package com.phenikaa.authservice.service;


import com.phenikaa.authservice.client.UserServiceClient;
import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.authservice.dto.response.AuthResponse;
import com.phenikaa.utils.JwtUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;

    public AuthService(UserServiceClient userServiceClient, JwtUtil jwtUtil) {
        this.userServiceClient = userServiceClient;
        this.jwtUtil = jwtUtil;
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        return userServiceClient.verifyUser(request)
                .doOnNext(user -> System.out.println("User from user-service: " + user))
                .switchIfEmpty(Mono.error(new RuntimeException("User not found or password sai")))
                .flatMap(user -> {
                    System.out.println("User roles: " + user.roles());
                    System.out.println("Requested role: " + request.getRole());

                    if (!user.roles().contains(request.getRole())) {
                        return Mono.error(new RuntimeException("Role không hợp lệ"));
                    }

                    var authorities = user.roles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    String accessToken = jwtUtil.generateAccessToken(
                            user.username(), user.id(), authorities
                    );

                    System.out.println("Generated token: " + accessToken);

                    return Mono.just(new AuthResponse(accessToken));
                });
    }


}

