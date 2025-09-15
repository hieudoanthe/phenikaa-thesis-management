package com.phenikaa.statisticsservice.controller;

import com.phenikaa.statisticsservice.dto.response.AdminStatisticsResponse;
import com.phenikaa.statisticsservice.dto.response.TeacherStatisticsResponse;
import com.phenikaa.statisticsservice.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics-service")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    /**
     * Lấy thống kê tổng quan cho Admin (main endpoint)
     * GET /api/statistics-service/admin/statistics
     */
    @GetMapping("/admin/statistics")
    public ResponseEntity<AdminStatisticsResponse> getAdminStatistics() {
        log.info("Getting admin statistics");
        try {
            AdminStatisticsResponse response = statisticsService.getAdminStatistics();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting admin statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê tổng quan cho Admin (alternative endpoint)
     * GET /api/statistics-service/admin/overview
     */
    @GetMapping("/admin/overview")
    public ResponseEntity<AdminStatisticsResponse> getAdminOverview() {
        log.info("Getting admin overview statistics");
        try {
            AdminStatisticsResponse response = statisticsService.getAdminStatistics();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting admin overview: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê theo khoảng thời gian cho Admin
     * GET /api/statistics/admin/date-range?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/admin/date-range")
    public ResponseEntity<AdminStatisticsResponse> getAdminStatisticsByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Getting admin statistics for date range: {} to {}", startDate, endDate);
        try {
            // TODO: Implement date range filtering
            AdminStatisticsResponse response = statisticsService.getAdminStatistics();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting admin date range statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê theo năm học cho Admin
     * GET /api/statistics/admin/academic-year/{yearId}
     */
    @GetMapping("/admin/academic-year/{yearId}")
    public ResponseEntity<AdminStatisticsResponse> getAdminStatisticsByAcademicYear(
            @PathVariable Integer yearId) {
        log.info("Getting admin statistics for academic year: {}", yearId);
        try {
            // TODO: Implement academic year filtering
            AdminStatisticsResponse response = statisticsService.getAdminStatistics();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting admin academic year statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê real-time cho Admin
     * GET /api/statistics/admin/realtime
     */
    @GetMapping("/admin/realtime")
    public ResponseEntity<AdminStatisticsResponse> getAdminRealtimeStatistics() {
        log.info("Getting admin realtime statistics");
        try {
            AdminStatisticsResponse response = statisticsService.getAdminStatistics();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting admin realtime statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê nhanh cho Admin - đề tài đăng ký hôm nay
     * GET /api/statistics-service/admin/quick-stats/today-registrations
     */
    @GetMapping("/admin/quick-stats/today-registrations")
    public ResponseEntity<Map<String, Object>> getTodayRegistrations() {
        log.info("Getting today's registrations");
        try {
            Map<String, Object> response = statisticsService.getTodayRegistrations();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting today's registrations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy thống kê nhanh cho Admin - đề tài nộp hôm nay
     * GET /api/statistics-service/admin/quick-stats/today-submissions
     */
    @GetMapping("/admin/quick-stats/today-submissions")
    public ResponseEntity<Map<String, Object>> getTodaySubmissions() {
        log.info("Getting today's submissions");
        try {
            Map<String, Object> response = statisticsService.getTodaySubmissions();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting today's submissions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy thống kê nhanh cho Admin - đánh giá chờ xử lý
     * GET /api/statistics-service/admin/quick-stats/pending-evaluations
     */
    @GetMapping("/admin/quick-stats/pending-evaluations")
    public ResponseEntity<Map<String, Object>> getPendingEvaluations() {
        log.info("Getting pending evaluations");
        try {
            Map<String, Object> response = statisticsService.getPendingEvaluations();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting pending evaluations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy thống kê nhanh cho Admin - hoạt động hôm nay
     * GET /api/statistics-service/admin/quick-stats/today-activity
     */
    @GetMapping("/admin/quick-stats/today-activity")
    public ResponseEntity<Map<String, Object>> getTodayActivity() {
        log.info("Getting today's activity");
        try {
            Map<String, Object> response = statisticsService.getTodayActivity();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting today's activity: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy thống kê cho Giảng viên (main endpoint)
     * GET /api/statistics-service/teacher/statistics/{teacherId}
     */
    @GetMapping("/teacher/statistics/{teacherId}")
    public ResponseEntity<TeacherStatisticsResponse> getTeacherStatistics(
            @PathVariable Integer teacherId) {
        log.info("Getting teacher statistics for teacher: {}", teacherId);
        try {
            TeacherStatisticsResponse response = statisticsService.getTeacherStatistics(teacherId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting teacher statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê tổng quan cho Giảng viên (alternative endpoint)
     * GET /api/statistics-service/teacher/{teacherId}/overview
     */
    @GetMapping("/teacher/{teacherId}/overview")
    public ResponseEntity<TeacherStatisticsResponse> getTeacherOverview(
            @PathVariable Integer teacherId) {
        log.info("Getting teacher overview statistics for teacher: {}", teacherId);
        try {
            TeacherStatisticsResponse response = statisticsService.getTeacherStatistics(teacherId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting teacher overview: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê theo khoảng thời gian cho Giảng viên
     * GET /api/statistics/teacher/{teacherId}/date-range?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/teacher/{teacherId}/date-range")
    public ResponseEntity<TeacherStatisticsResponse> getTeacherStatisticsByDateRange(
            @PathVariable Integer teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Getting teacher statistics for teacher: {} and date range: {} to {}", teacherId, startDate, endDate);
        try {
            // TODO: Implement date range filtering
            TeacherStatisticsResponse response = statisticsService.getTeacherStatistics(teacherId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting teacher date range statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê theo năm học cho Giảng viên
     * GET /api/statistics/teacher/{teacherId}/academic-year/{yearId}
     */
    @GetMapping("/teacher/{teacherId}/academic-year/{yearId}")
    public ResponseEntity<TeacherStatisticsResponse> getTeacherStatisticsByAcademicYear(
            @PathVariable Integer teacherId,
            @PathVariable Integer yearId) {
        log.info("Getting teacher statistics for teacher: {} and academic year: {}", teacherId, yearId);
        try {
            // TODO: Implement academic year filtering
            TeacherStatisticsResponse response = statisticsService.getTeacherStatistics(teacherId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting teacher academic year statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê real-time cho Giảng viên
     * GET /api/statistics/teacher/{teacherId}/realtime
     */
    @GetMapping("/teacher/{teacherId}/realtime")
    public ResponseEntity<TeacherStatisticsResponse> getTeacherRealtimeStatistics(
            @PathVariable Integer teacherId) {
        log.info("Getting teacher realtime statistics for teacher: {}", teacherId);
        try {
            TeacherStatisticsResponse response = statisticsService.getTeacherStatistics(teacherId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting teacher realtime statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê workload cho Giảng viên
     * GET /api/statistics/teacher/{teacherId}/workload
     */
    @GetMapping("/teacher/{teacherId}/workload")
    public ResponseEntity<TeacherStatisticsResponse> getTeacherWorkload(
            @PathVariable Integer teacherId) {
        log.info("Getting teacher workload statistics for teacher: {}", teacherId);
        try {
            TeacherStatisticsResponse response = statisticsService.getTeacherStatistics(teacherId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting teacher workload statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê performance cho Giảng viên
     * GET /api/statistics/teacher/{teacherId}/performance
     */
    @GetMapping("/teacher/{teacherId}/performance")
    public ResponseEntity<TeacherStatisticsResponse> getTeacherPerformance(
            @PathVariable Integer teacherId) {
        log.info("Getting teacher performance statistics for teacher: {}", teacherId);
        try {
            TeacherStatisticsResponse response = statisticsService.getTeacherStatistics(teacherId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting teacher performance statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê sinh viên cho Giảng viên
     * GET /api/statistics/teacher/{teacherId}/students
     */
    @GetMapping("/teacher/{teacherId}/students")
    public ResponseEntity<TeacherStatisticsResponse> getTeacherStudents(
            @PathVariable Integer teacherId) {
        log.info("Getting teacher students statistics for teacher: {}", teacherId);
        try {
            TeacherStatisticsResponse response = statisticsService.getTeacherStatistics(teacherId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting teacher students statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê đề tài cho Giảng viên
     * GET /api/statistics/teacher/{teacherId}/topics
     */
    @GetMapping("/teacher/{teacherId}/topics")
    public ResponseEntity<TeacherStatisticsResponse> getTeacherTopics(
            @PathVariable Integer teacherId) {
        log.info("Getting teacher topics statistics for teacher: {}", teacherId);
        try {
            TeacherStatisticsResponse response = statisticsService.getTeacherStatistics(teacherId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting teacher topics statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
