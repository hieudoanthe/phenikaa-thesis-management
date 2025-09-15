package com.phenikaa.statisticsservice.client;

import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "api-gateway",
        contextId = "userServiceClient",
        path = "/user-service",
        configuration = FeignTokenInterceptor.class)
public interface UserServiceClient {
    
    @GetMapping("/internal/users/get-all-users")
    List<GetUserResponse> getAllUsers();
    
    @GetMapping("/internal/users/get-users-by-role")
    List<GetUserResponse> getUsersByRole(@RequestParam String role);
    
    @GetMapping("/internal/users/get-user-count")
    Long getUserCount();
    
    @GetMapping("/internal/users/get-user-count-by-role")
    Long getUserCountByRole(@RequestParam String role);
    
    @GetMapping("/internal/users/get-user-count-by-status")
    Long getUserCountByStatus(@RequestParam Integer status);
    
    @GetMapping("/internal/users/get-active-users-today")
    Long getActiveUsersToday();
}
