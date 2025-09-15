package com.phenikaa.submissionservice.controller;

import com.phenikaa.submissionservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/submissions")
@RequiredArgsConstructor
@Slf4j
public class InternalStatisticsController {
    
    private final AnalyticsService analyticsService;
    
    @GetMapping("/get-submission-count")
    public Long getSubmissionCount() {
        log.info("Getting total submission count");
        return analyticsService.getSubmissionCount();
    }
    
    @GetMapping("/get-submission-count-by-status")
    public Long getSubmissionCountByStatus(@RequestParam Integer status) {
        log.info("Getting submission count by status: {}", status);
        return analyticsService.getSubmissionCountByStatus(status);
    }
    
    @GetMapping("/get-submission-count-by-topic")
    public Long getSubmissionCountByTopic(@RequestParam Integer topicId) {
        log.info("Getting submission count by topic: {}", topicId);
        return analyticsService.getSubmissionCountByTopic(topicId);
    }
    
    @GetMapping("/get-submission-count-by-user")
    public Long getSubmissionCountByUser(@RequestParam Integer userId) {
        log.info("Getting submission count by user: {}", userId);
        return analyticsService.getSubmissionCountByUser(userId);
    }
    
    @GetMapping("/get-submissions-over-time")
    public List<Map<String, Object>> getSubmissionsOverTime(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("Getting submissions over time from {} to {}", startDate, endDate);
        return analyticsService.getSubmissionsOverTime(startDate, endDate);
    }
    
    @GetMapping("/get-submissions-by-topic")
    public List<Map<String, Object>> getSubmissionsByTopic(@RequestParam Integer topicId) {
        log.info("Getting submissions by topic: {}", topicId);
        return analyticsService.getSubmissionsByTopic(topicId);
    }
    
    @GetMapping("/get-submissions-by-user")
    public List<Map<String, Object>> getSubmissionsByUser(@RequestParam Integer userId) {
        log.info("Getting submissions by user: {}", userId);
        return analyticsService.getSubmissionsByUser(userId);
    }
    
    @GetMapping("/get-deadline-stats")
    public Map<String, Long> getDeadlineStats() {
        log.info("Getting deadline statistics");
        return analyticsService.getDeadlineStats();
    }
    
    @GetMapping("/get-submissions-today")
    public Long getSubmissionsToday() {
        log.info("Getting submissions today");
        return analyticsService.getSubmissionsToday();
    }
    
    @GetMapping("/get-today-submissions")
    public List<Map<String, Object>> getTodaySubmissions() {
        log.info("Getting today's submissions");
        return analyticsService.getTodaySubmissions();
    }
}
