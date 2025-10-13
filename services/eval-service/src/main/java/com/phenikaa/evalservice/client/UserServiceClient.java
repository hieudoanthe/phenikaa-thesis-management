package com.phenikaa.evalservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "api-gateway",
        contextId = "userServiceClient",
        configuration = FeignTokenInterceptor.class)
public interface UserServiceClient {

    /**
     * Lấy thông tin user theo ID
     */
    @GetMapping("/api/user-service/internal/users/get-profile/{userId}")
    Map<String, Object> getUserById(@PathVariable("userId") Integer userId);

    /**
     * Lấy username theo ID
     */
    @GetMapping("/api/user-service/internal/users/get-username/{userId}")
    String getUsernameById(@PathVariable("userId") Integer userId);
}
