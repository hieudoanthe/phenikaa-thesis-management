package com.phenikaa.apigateway.config;

import com.phenikaa.apigateway.security.JwtAuthenticationManager;
import com.phenikaa.apigateway.security.ServerHttpBearerAuthenticationConverter;
import com.phenikaa.apigateway.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;

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
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> {})
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // API for Authentication
                        .pathMatchers("/api/auth/**").permitAll()
                        // API for All requests
                        .pathMatchers("/internal/users/**").permitAll()
                        .pathMatchers("/internal/periods/**").permitAll()
                        .pathMatchers("/internal/thesis/**").permitAll()
                        .pathMatchers("/internal/academic/**").permitAll()
                        .pathMatchers("/internal/assignments/**").permitAll()
                        .pathMatchers("/internal/profiles/**").permitAll()
                        .pathMatchers("/internal/submissions/**").permitAll()
                        .pathMatchers("/internal/evaluations/**").permitAll()

                        .pathMatchers("/api/statistics-service/**").hasAnyRole("TEACHER", "ADMIN")
                        //
                        .pathMatchers("/api/user-service/admin/**").hasAnyRole("ADMIN", "TEACHER")
                        .pathMatchers("/api/eval-service/admin/**").hasRole("ADMIN")
                        .pathMatchers("/api/eval-service/student/**").hasRole("STUDENT")
                        .pathMatchers( "/api/thesis-service/ai-chat/**").hasRole("STUDENT")
                        .pathMatchers("/api/eval-service/teacher/**").hasRole("TEACHER")
                        .pathMatchers("/api/thesis-service/student-period/**").hasAnyRole("ADMIN", "STUDENT")
                        .pathMatchers("/api/user-service/student/**").hasRole("STUDENT")
                        .pathMatchers("/api/thesis-service/student/**").hasRole("STUDENT")
                        .pathMatchers("/api/thesis-service/teacher/**").hasRole("TEACHER")
                        .pathMatchers("/api/thesis-service/admin/**").hasAnyRole("ADMIN", "STUDENT")
                        .pathMatchers("/api/profile-service/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                        .pathMatchers("/api/assign-service/**").hasAnyRole("STUDENT", "TEACHER")
                        .pathMatchers("/api/academic-config-service/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                        .pathMatchers("/api/communication-log-service/**").permitAll()
                        .pathMatchers("/api/communication-service/**").permitAll()
                        // API of Chat and Notifications
                        .pathMatchers("/api/submission-service/**").permitAll()
                        .pathMatchers("/notifications/**").permitAll()
                        .pathMatchers("/chat/**").permitAll()
                        // Chat and Notifications by WebSocket
                        .pathMatchers("/ws/chat/**").permitAll()
                        .pathMatchers("/ws/notifications/**").permitAll()
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

    @Bean
    @org.springframework.core.annotation.Order(-1)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("https://phenikaa-thesis-management-fe.vercel.app/");
        config.addAllowedOrigin("https://websocketking.com");
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

}
