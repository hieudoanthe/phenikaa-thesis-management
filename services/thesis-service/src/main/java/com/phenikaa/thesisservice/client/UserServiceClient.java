package com.phenikaa.thesisservice.client;

import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "api-gateway",
        contextId = "thesisUserServiceClient",
        path = "/user-service",
        configuration = FeignTokenInterceptor.class
)
public interface UserServiceClient {

    @GetMapping("/internal/users/by-role")
    List<GetUserResponse> getUsersByRole(@RequestParam("role") String role);

    @GetMapping("/internal/users/students/by-period/{periodId}")
    List<java.util.Map<String, Object>> getStudentsByPeriod(@PathVariable("periodId") Integer periodId);
}


