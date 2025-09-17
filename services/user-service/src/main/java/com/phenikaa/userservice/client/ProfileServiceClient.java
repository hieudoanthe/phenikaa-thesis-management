package com.phenikaa.userservice.client;

import com.phenikaa.dto.request.CreateProfileRequest;
import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

//@FeignClient(
//        name = "api-gateway",
//        contextId = "profileServiceClient",
//        path = "/profile-service",
//        configuration = FeignTokenInterceptor.class
//)
//public interface ProfileServiceClient {
//
//    @PostMapping("/api/profile-service/admin/create-profile")
//    void createProfile(@RequestBody CreateProfileRequest request);
//
//    @DeleteMapping("/api/profile-service/admin/delete-profile/{userId}")
//    void deleteProfile(@PathVariable("userId") Integer userId);
//}
@FeignClient(
        name = "api-gateway",
        contextId = "profileServiceClient",
        url = "${API_GATEWAY_URL:https://api-gateway-production-95a5.up.railway.app}",
        path = "/api/profile-service",
        configuration = FeignTokenInterceptor.class
)
public interface ProfileServiceClient {

    @PostMapping("/admin/create-profile")
    void createProfile(@RequestBody CreateProfileRequest request);

    @DeleteMapping("/admin/delete-profile/{userId}")
    void deleteProfile(@PathVariable("userId") Integer userId);
}
