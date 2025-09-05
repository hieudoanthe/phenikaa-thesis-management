package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.dto.response.StatisticsOverviewResponse;
import com.phenikaa.evalservice.dto.response.DefenseStatisticsResponse;
import com.phenikaa.evalservice.dto.response.EvaluationStatisticsResponse;
import com.phenikaa.evalservice.dto.response.ScoreStatisticsResponse;
import com.phenikaa.evalservice.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/eval-service/admin/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    /**
     * Lấy thống kê tổng quan
     */
    @GetMapping("/overview")
    public ResponseEntity<StatisticsOverviewResponse> getOverviewStatistics() {
        log.info("Getting overview statistics");
        try {
            StatisticsOverviewResponse response = statisticsService.getOverviewStatistics();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting overview statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê buổi bảo vệ
     */
    @GetMapping("/defenses")
    public ResponseEntity<DefenseStatisticsResponse> getDefenseStatistics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        log.info("Getting defense statistics from {} to {}", startDate, endDate);
        try {
            DefenseStatisticsResponse response = statisticsService.getDefenseStatistics(startDate, endDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting defense statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê đánh giá
     */
    @GetMapping("/evaluations")
    public ResponseEntity<EvaluationStatisticsResponse> getEvaluationStatistics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        log.info("Getting evaluation statistics from {} to {}", startDate, endDate);
        try {
            EvaluationStatisticsResponse response = statisticsService.getEvaluationStatistics(startDate, endDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting evaluation statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê điểm số
     */
    @GetMapping("/scores")
    public ResponseEntity<ScoreStatisticsResponse> getScoreStatistics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        log.info("Getting score statistics from {} to {}", startDate, endDate);
        try {
            ScoreStatisticsResponse response = statisticsService.getScoreStatistics(startDate, endDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting score statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê theo tháng
     */
    @GetMapping("/monthly")
    public ResponseEntity<List<Object>> getMonthlyStatistics(
            @RequestParam(required = false) Integer year) {
        log.info("Getting monthly statistics for year: {}", year);
        try {
            List<Object> response = statisticsService.getMonthlyStatistics(year);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting monthly statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
