package com.phenikaa.communicationservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }

    public Mono<String> getUsernameById(Integer userId) {
        return webClient
                .get()
                .uri("/internal/users/get-username/{userId}", userId)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("");
    }

    @SuppressWarnings("unchecked")
    public Flux<Map<String, Object>> getUsersByIds(String... userIds) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/users/batch")
                        .queryParam("ids", String.join(",", userIds))
                        .build())
                .retrieve()
                .bodyToFlux(Map.class)
                .cast(Map.class)
                .map(map -> {
                    Map<String, Object> result = new HashMap<>();
                    map.forEach((k, v) -> result.put(String.valueOf(k), v));
                    return result;
                })
                .onErrorReturn(Map.of());
    }

    /**
     * Lấy danh sách username sinh viên trong đợt đăng ký
     */
    public Flux<String> getStudentsByPeriod(Long periodId) {
        return webClient
                .get()
                .uri("/internal/users/students/by-period/{periodId}", periodId)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMapMany(wrapper -> {
                    try {
                        System.out.println("[UserServiceClient] wrapper keys: " + wrapper.keySet());
                    } catch (Exception ignored) {}

                    Object data = wrapper.get("data");
                    if (!(data instanceof Iterable<?> iterable)) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(iterable)
                            .cast(Map.class)
                            .map(item -> {
                                try {
                                    System.out.println("[UserServiceClient] item keys: " + item.keySet());
                                } catch (Exception ignored) {}
                                Object username = item.get("username");
                                return username == null ? "" : username.toString();
                            })
                            .filter(s -> s != null && !s.isBlank());
                })
                .onErrorResume(err -> {
                    System.out.println("[UserServiceClient] error: " + err.getMessage());
                    return Flux.empty();
                });
    }
}