package com.phenikaa.evalservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "api-gateway",
        contextId = "profileServiceClient",
        path = "/profile-service",
        configuration = FeignTokenInterceptor.class)
public interface ProfileServiceClient {

    /**
     * Lấy thông tin profile sinh viên theo userId
     */
    @GetMapping("/api/profile-service/student/get-profile/{userId}")
    Map<String, Object> getStudentProfile(@PathVariable("userId") Integer userId);

    /**
     * Lấy thông tin profile giảng viên theo userId
     */
    @GetMapping("/api/profile-service/teacher/get-profile/{userId}")
    Map<String, Object> getTeacherProfile(@PathVariable("userId") Integer userId);
}
