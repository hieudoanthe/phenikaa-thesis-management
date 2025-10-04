package com.phenikaa.submissionservice.client;

import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(
        name = "api-gateway",
        contextId = "submissionServiceClient",
        path = "/user-service",
        configuration = FeignTokenInterceptor.class
)
public interface UserServiceClient {
    @GetMapping("/internal/users/get-profile/{userId}")
    GetUserResponse getUserById(@PathVariable("userId") Integer userId);

    @GetMapping("/internal/users/get-username/{userId}")
    String getUsernameById(@PathVariable("userId") Integer userId);
}