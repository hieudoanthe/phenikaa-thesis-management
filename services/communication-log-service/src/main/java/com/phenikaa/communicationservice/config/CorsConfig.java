package com.phenikaa.communicationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow all origins for development
        corsConfig.addAllowedOriginPattern("*");
        
        // Allow all HTTP methods including OPTIONS
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        
        // Allow all headers
        corsConfig.addAllowedHeader("*");
        
        // Allow credentials
        corsConfig.setAllowCredentials(true);
        
        // Expose headers
        corsConfig.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        
        // Max age for preflight requests
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}
