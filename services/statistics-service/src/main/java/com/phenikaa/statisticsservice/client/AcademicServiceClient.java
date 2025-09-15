package com.phenikaa.statisticsservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "api-gateway",
        contextId = "academicServiceClient",
        path = "/academic-config-service",
        configuration = FeignTokenInterceptor.class)
public interface AcademicServiceClient {
    
    @GetMapping("/internal/academic/get-academic-years")
    List<Map<String, Object>> getAcademicYears();
    
    @GetMapping("/internal/academic/get-active-academic-year")
    Map<String, Object> getActiveAcademicYear();
    
    @GetMapping("/internal/academic/get-academic-year-count")
    Long getAcademicYearCount();
    
    @GetMapping("/internal/academic/get-academic-year-by-id")
    Map<String, Object> getAcademicYearById(@RequestParam Integer yearId);
}
