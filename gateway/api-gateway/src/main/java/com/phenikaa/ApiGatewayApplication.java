package com.phenikaa;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(ApiGatewayApplication.class, args);
    }
}