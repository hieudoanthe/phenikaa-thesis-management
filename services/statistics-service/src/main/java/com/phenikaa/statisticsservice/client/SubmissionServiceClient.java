package com.phenikaa.statisticsservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "api-gateway",
        contextId = "submissionServiceClient",
        path = "/submission-service",
        configuration = FeignTokenInterceptor.class)
public interface SubmissionServiceClient {
    
    @GetMapping("/internal/submissions/get-submission-count")
    Long getSubmissionCount();
    
    @GetMapping("/internal/submissions/get-submission-count-by-status")
    Long getSubmissionCountByStatus(@RequestParam Integer status);
    
    @GetMapping("/internal/submissions/get-submission-count-by-topic")
    Long getSubmissionCountByTopic(@RequestParam Integer topicId);
    
    @GetMapping("/internal/submissions/get-submission-count-by-user")
    Long getSubmissionCountByUser(@RequestParam Integer userId);
    
    @GetMapping("/internal/submissions/get-submissions-over-time")
    List<Map<String, Object>> getSubmissionsOverTime(@RequestParam(required = false) String startDate, 
                                                    @RequestParam(required = false) String endDate);
    
    @GetMapping("/internal/submissions/get-submissions-by-topic")
    List<Map<String, Object>> getSubmissionsByTopic(@RequestParam Integer topicId);
    
    @GetMapping("/internal/submissions/get-submissions-by-user")
    List<Map<String, Object>> getSubmissionsByUser(@RequestParam Integer userId);
    
    @GetMapping("/internal/submissions/get-deadline-stats")
    Map<String, Long> getDeadlineStats();
    
    @GetMapping("/internal/submissions/get-submissions-today")
    Long getSubmissionsToday();
    
    @GetMapping("/internal/submissions/get-today-submissions")
    List<Map<String, Object>> getTodaySubmissions();
}
