package com.phenikaa.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
@Slf4j
public class AuthenticationHeaderRelayFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(auth -> {
                    String username = auth.getName();
                    String roles = auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(","));

                    log.info("Relay headers → X-Username={}, X-Roles={}, X-Internal-Secret=abc123", username, roles);

                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-Username", username)
                            .header("X-Roles", roles)
                            .header("X-Internal-Secret", "abc123")
                            .build();

                    ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                    return chain.filter(mutatedExchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }
}
