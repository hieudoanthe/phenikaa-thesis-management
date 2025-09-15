package com.phenikaa.statisticsservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "api-gateway",
        contextId = "thesisServiceClient",
        path = "/thesis-service",
        configuration = FeignTokenInterceptor.class)
public interface ThesisServiceClient {
    
    @GetMapping("/internal/thesis/get-topic-count")
    Long getTopicCount();
    
    @GetMapping("/internal/thesis/get-topic-count-by-status")
    Long getTopicCountByStatus(@RequestParam String status);
    
    @GetMapping("/internal/thesis/get-topic-count-by-difficulty")
    Long getTopicCountByDifficulty(@RequestParam String difficulty);
    
    @GetMapping("/internal/thesis/get-topic-count-by-academic-year")
    Long getTopicCountByAcademicYear(@RequestParam Integer academicYearId);
    
    @GetMapping("/internal/thesis/get-topic-count-by-supervisor")
    Long getTopicCountBySupervisor(@RequestParam Integer supervisorId);
    
    @GetMapping("/internal/thesis/get-registration-count")
    Long getRegistrationCount();
    
    @GetMapping("/internal/thesis/get-registration-count-by-status")
    Long getRegistrationCountByStatus(@RequestParam String status);
    
    @GetMapping("/internal/thesis/get-registration-count-by-academic-year")
    Long getRegistrationCountByAcademicYear(@RequestParam Integer academicYearId);
    
    @GetMapping("/internal/thesis/get-topics-by-supervisor")
    List<Map<String, Object>> getTopicsBySupervisor(@RequestParam Integer supervisorId);
    
    @GetMapping("/internal/thesis/get-topics-stats-by-supervisor")
    Map<String, Object> getTopicsStatsBySupervisor(@RequestParam Integer supervisorId);
    
    @GetMapping("/internal/thesis/get-registrations-by-topic")
    List<Map<String, Object>> getRegistrationsByTopic(@RequestParam Integer topicId);
    
    @GetMapping("/internal/thesis/get-topics-over-time")
    List<Map<String, Object>> getTopicsOverTime(@RequestParam(required = false) String startDate, 
                                               @RequestParam(required = false) String endDate);
    
    @GetMapping("/internal/thesis/get-registrations-over-time")
    List<Map<String, Object>> getRegistrationsOverTime(@RequestParam(required = false) String startDate, 
                                                      @RequestParam(required = false) String endDate);
    
    @GetMapping("/internal/thesis/get-registrations-today")
    Long getRegistrationsToday();
    
    @GetMapping("/internal/thesis/get-today-registrations")
    List<Map<String, Object>> getTodayRegistrations();
}
