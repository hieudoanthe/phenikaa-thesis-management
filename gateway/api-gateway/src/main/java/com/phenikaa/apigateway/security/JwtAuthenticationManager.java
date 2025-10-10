package com.phenikaa.apigateway.security;

import com.phenikaa.apigateway.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

// Xác thực một Authentication (được tạo ra bởi converter)
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();

        if (!jwtUtil.validateToken(token)) {
            return Mono.empty();
        }

        String username = jwtUtil.getUsernameFromJWT(token);
        List<String> roles = jwtUtil.getRoles(token);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
        return Mono.just(auth);
    }
}
