package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.service.PeriodStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/period-statistics")
public class PeriodStatisticsController {

    private final PeriodStatisticsService periodStatisticsService;

    public PeriodStatisticsController(PeriodStatisticsService periodStatisticsService) {
        this.periodStatisticsService = periodStatisticsService;
    }

    /**
     * Lấy thống kê tổng quan của một đợt đăng ký
     */
    @GetMapping("/{periodId}/overview")
    public ResponseEntity<?> getPeriodOverview(@PathVariable Integer periodId) {
        try {
            var response = periodStatisticsService.getPeriodOverview(periodId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy tổng số sinh viên của một đợt (đã đăng ký + chưa đăng ký)
     */
    @GetMapping("/{periodId}/total-students")
    public ResponseEntity<?> getTotalStudents(@PathVariable Integer periodId) {
        try {
            Integer totalStudents = periodStatisticsService.getTotalStudents(periodId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "periodId", periodId,
                "totalStudents", totalStudents,
                "message", "Lấy tổng số sinh viên thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Lấy số sinh viên đã đăng ký/đề xuất của một đợt
     */
    @GetMapping("/{periodId}/registered-students")
    public ResponseEntity<?> getRegisteredStudents(@PathVariable Integer periodId) {
        try {
            Integer registeredStudents = periodStatisticsService.getRegisteredStudents(periodId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "periodId", periodId,
                "registeredStudents", registeredStudents,
                "message", "Lấy số sinh viên đã đăng ký thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Lấy số sinh viên chưa đăng ký/đề xuất của một đợt
     */
    @GetMapping("/{periodId}/unregistered-students")
    public ResponseEntity<?> getUnregisteredStudents(@PathVariable Integer periodId) {
        try {
            Integer unregisteredStudents = periodStatisticsService.getUnregisteredStudents(periodId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "periodId", periodId,
                "unregisteredStudents", unregisteredStudents,
                "message", "Lấy số sinh viên chưa đăng ký thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
