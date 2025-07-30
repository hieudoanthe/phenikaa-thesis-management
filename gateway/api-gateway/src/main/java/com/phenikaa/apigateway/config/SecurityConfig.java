package com.phenikaa.apigateway.config;

import com.phenikaa.apigateway.security.JwtAuthenticationManager;
import com.phenikaa.apigateway.security.ServerHttpBearerAuthenticationConverter;
import com.phenikaa.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.config.web.server.ServerHttpSecurity;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         JwtAuthenticationManager jwtAuthManager,
                                                         ServerHttpBearerAuthenticationConverter authConverter) {

        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(jwtAuthManager);
        authenticationWebFilter.setServerAuthenticationConverter(authConverter);

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")
                        .pathMatchers("/api/user/**").hasRole("USER")
                        .pathMatchers("/api/teacher/**").hasRole("TEACHER")
                        .pathMatchers("/api/lecturer/thesis/**").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public JwtAuthenticationManager jwtAuthenticationManager(JwtUtil jwtUtil) {
        return new JwtAuthenticationManager(jwtUtil);
    }

    @Bean
    public ServerHttpBearerAuthenticationConverter bearerAuthenticationConverter() {
        return new ServerHttpBearerAuthenticationConverter();
    }

}
