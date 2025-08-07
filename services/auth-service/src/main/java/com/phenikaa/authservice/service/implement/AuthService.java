package com.phenikaa.authservice.service.implement;


import com.phenikaa.authservice.client.UserServiceClient;
import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.authservice.dto.response.AuthResponse;
import com.phenikaa.dto.request.RefreshTokenRequest;
import com.phenikaa.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;

    public Mono<AuthResponse> login(LoginRequest request) {
        return userServiceClient.verifyUser(request)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found or password wrong!")))
                .flatMap(user -> {
                    if (!user.roles().contains(request.getRole())) {
                        return Mono.error(new RuntimeException("Role does not match!"));
                    }

                    var authorities = user.roles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    String accessToken = jwtUtil.generateAccessToken(user.username(), user.id(), authorities);
                    String refreshToken = jwtUtil.generateRefreshToken(user.username(), user.id());

                    RefreshTokenRequest tokenRequest = new RefreshTokenRequest();
                    tokenRequest.setToken(refreshToken);
                    tokenRequest.setUserId(user.id());
                    tokenRequest.setExpiryDate(jwtUtil.getExpirationDateFromToken(refreshToken).toInstant());

                    return userServiceClient.saveRefreshToken(tokenRequest)
                            .thenReturn(new AuthResponse(accessToken, refreshToken));
                });
    }


}

