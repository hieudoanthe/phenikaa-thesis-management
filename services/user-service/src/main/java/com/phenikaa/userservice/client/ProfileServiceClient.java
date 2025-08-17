package com.phenikaa.userservice.client;

import com.phenikaa.dto.request.CreateProfileRequest;
import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "api-gateway", path = "/profile-service", configuration = FeignTokenInterceptor.class)
public interface ProfileServiceClient {

    @PostMapping("/api/profile-service/admin/create-profile")
    void createProfile(@RequestBody CreateProfileRequest request);

    @DeleteMapping("/api/profile-service/admin/delete-profile/{userId}")
    void deleteProfile(@PathVariable("userId") Integer userId);
}
