package com.phenikaa.statisticsservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "api-gateway",
        contextId = "profileServiceClient",
        path = "/profile-service",
        configuration = FeignTokenInterceptor.class)
public interface ProfileServiceClient {
    
    @GetMapping("/internal/profiles/get-profile-count")
    Long getProfileCount();
    
    @GetMapping("/internal/profiles/get-student-profile-count")
    Long getStudentProfileCount();
    
    @GetMapping("/internal/profiles/get-lecturer-profile-count")
    Long getLecturerProfileCount();
    
    @GetMapping("/internal/profiles/get-profiles-by-major")
    List<Map<String, Object>> getProfilesByMajor(@RequestParam String major);
    
    @GetMapping("/internal/profiles/get-profiles-by-year")
    List<Map<String, Object>> getProfilesByYear(@RequestParam Integer year);
    
    @GetMapping("/internal/profiles/get-student-profiles-by-supervisor")
    List<Map<String, Object>> getStudentProfilesBySupervisor(@RequestParam Integer supervisorId);
    
    @GetMapping("/internal/profiles/get-profile-by-user-id")
    Map<String, Object> getProfileByUserId(@RequestParam Integer userId);
}
