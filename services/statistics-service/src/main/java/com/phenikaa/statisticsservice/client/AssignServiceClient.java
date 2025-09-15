package com.phenikaa.statisticsservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "api-gateway",
        contextId = "assignServiceClient",
        path = "/assign-service",
        configuration = FeignTokenInterceptor.class)
public interface AssignServiceClient {
    
    @GetMapping("/internal/assignments/get-assignment-count")
    Long getAssignmentCount();
    
    @GetMapping("/internal/assignments/get-assignment-count-by-status")
    Long getAssignmentCountByStatus(@RequestParam String status);
    
    @GetMapping("/internal/assignments/get-assignment-count-by-user")
    Long getAssignmentCountByUser(@RequestParam Integer userId);
    
    @GetMapping("/internal/assignments/get-assignment-count-by-topic")
    Long getAssignmentCountByTopic(@RequestParam Integer topicId);
    
    @GetMapping("/internal/assignments/get-assignments-by-user")
    List<Map<String, Object>> getAssignmentsByUser(@RequestParam Integer userId);
    
    @GetMapping("/internal/assignments/get-assignments-by-topic")
    List<Map<String, Object>> getAssignmentsByTopic(@RequestParam Integer topicId);
    
    @GetMapping("/internal/assignments/get-task-count")
    Long getTaskCount();
    
    @GetMapping("/internal/assignments/get-task-count-by-status")
    Long getTaskCountByStatus(@RequestParam String status);
    
    @GetMapping("/internal/assignments/get-task-count-by-assignment")
    Long getTaskCountByAssignment(@RequestParam Integer assignmentId);
    
    @GetMapping("/internal/assignments/get-tasks-by-assignment")
    List<Map<String, Object>> getTasksByAssignment(@RequestParam Integer assignmentId);
}
