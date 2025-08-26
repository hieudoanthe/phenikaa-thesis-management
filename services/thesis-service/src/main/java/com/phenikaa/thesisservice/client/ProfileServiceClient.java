package com.phenikaa.thesisservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(
        name = "api-gateway",
        contextId = "profileServiceClient",
        path = "/profile-service",
        configuration = FeignTokenInterceptor.class
)
public interface ProfileServiceClient {

    @PutMapping("/api/profile-service/teacher/decrease-capacity")
    void decreaseTeacherCapacity();
}


