package com.phenikaa.thesisservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.Map;

@FeignClient(
        name = "api-gateway",
        contextId = "profileServiceClient",
        configuration = FeignTokenInterceptor.class
)
public interface ProfileServiceClient {

    @PutMapping("/api/profile-service/teacher/decrease-capacity")
    void decreaseTeacherCapacity();
    
    @GetMapping("/api/profile-service//teacher/get-profile/{userId}")
    Map<String, Object> getLecturerById(@PathVariable("userId") Integer userId);
}


