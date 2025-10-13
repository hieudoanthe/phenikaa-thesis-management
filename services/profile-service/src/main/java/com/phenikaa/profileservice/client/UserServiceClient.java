package com.phenikaa.profileservice.client;

import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "api-gateway", configuration = FeignTokenInterceptor.class)
public interface UserServiceClient {
    @GetMapping("/internal/users/get-profile/{userId}")
    GetUserResponse getUserById(@PathVariable("userId") Integer userId);

    @PostMapping("/internal/users/get-all-users/batch")
    List<GetUserResponse> getUsersByIds(@RequestBody List<Integer> userIds);
}
