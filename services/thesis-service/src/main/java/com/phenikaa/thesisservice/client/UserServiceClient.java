package com.phenikaa.thesisservice.client;

import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.filter.FeignTokenInterceptor;
import com.phenikaa.thesisservice.dto.response.StudentsByPeriodResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "api-gateway",
        contextId = "userServiceClient",
        configuration = FeignTokenInterceptor.class
)
public interface UserServiceClient {

    @GetMapping("/internal/users/by-role")
    List<GetUserResponse> getUsersByRole(@RequestParam("role") String role);

    @GetMapping("/internal/users/students/by-period/{periodId}")
    StudentsByPeriodResponse getStudentsByPeriod(@PathVariable("periodId") Integer periodId);

    @GetMapping("/internal/users/get-profile/{userId}")
    GetUserResponse getUserById(@PathVariable("userId") Integer userId);

    @GetMapping("/internal/users/get-username/{userId}")
    String getUsernameById(@PathVariable("userId") Integer userId);
}


