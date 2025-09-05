package com.phenikaa.submissionservice.controller;

import com.phenikaa.submissionservice.dto.response.AnalyticsResponse;
import com.phenikaa.submissionservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/submission-service/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    /**
     * Lấy thống kê tổng quan
     */
    @GetMapping("/overview")
    public ResponseEntity<AnalyticsResponse> getOverallAnalytics() {
        try {
            log.info("Getting overall analytics");
            AnalyticsResponse response = analyticsService.getOverallAnalytics();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting overall analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Lấy thống kê theo khoảng thời gian
     */
    @GetMapping("/date-range")
    public ResponseEntity<AnalyticsResponse> getAnalyticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            log.info("Getting analytics for date range: {} to {}", startDate, endDate);
            AnalyticsResponse response = analyticsService.getAnalyticsByDateRange(startDate, endDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting date range analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Lấy thống kê theo người dùng
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<AnalyticsResponse> getUserAnalytics(@PathVariable Integer userId) {
        try {
            log.info("Getting analytics for user: {}", userId);
            AnalyticsResponse response = analyticsService.getUserAnalytics(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting user analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Lấy thống kê theo đề tài
     */
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<AnalyticsResponse> getTopicAnalytics(@PathVariable Integer topicId) {
        try {
            log.info("Getting analytics for topic: {}", topicId);
            AnalyticsResponse response = analyticsService.getTopicAnalytics(topicId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting topic analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Lấy thống kê real-time
     */
    @GetMapping("/realtime")
    public ResponseEntity<AnalyticsResponse> getRealtimeAnalytics() {
        try {
            log.info("Getting realtime analytics");
            // Lấy thống kê cho ngày hiện tại
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
            
            AnalyticsResponse response = analyticsService.getAnalyticsByDateRange(startOfDay, endOfDay);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting realtime analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
