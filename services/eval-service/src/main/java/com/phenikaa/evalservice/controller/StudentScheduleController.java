package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.dto.StudentScheduleDto;
import com.phenikaa.evalservice.service.StudentScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eval-service/student")
@RequiredArgsConstructor
@Slf4j
public class StudentScheduleController {

    private final StudentScheduleService studentScheduleService;

    /**
     * Lấy lịch trình của sinh viên theo ID
     */
    @GetMapping("/{studentId}/schedule")
    public ResponseEntity<List<StudentScheduleDto>> getStudentSchedule(@PathVariable Integer studentId) {
        try {
            List<StudentScheduleDto> schedule = studentScheduleService.getStudentSchedule(studentId);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch trình sinh viên ID {}: ", studentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy lịch trình sinh viên theo khoảng thời gian
     */
    @GetMapping("/{studentId}/schedule/date-range")
    public ResponseEntity<List<StudentScheduleDto>> getStudentScheduleByDateRange(
            @PathVariable Integer studentId,
            @RequestParam(defaultValue = "2024-01-01") String startDate,
            @RequestParam(defaultValue = "2025-12-31") String endDate) {
        try {
            List<StudentScheduleDto> schedule = studentScheduleService.getStudentScheduleByDateScope(studentId, startDate, endDate);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch trình sinh viên ID {} theo khoảng thời gian: ", studentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy lịch trình sinh viên sắp tới (30 ngày tiếp theo)
     */
    @GetMapping("/{studentId}/schedule/upcoming")
    public ResponseEntity<List<StudentScheduleDto>> getUpcomingSchedule(@PathVariable Integer studentId) {
        try {
            List<StudentScheduleDto> schedule = studentScheduleService.getUpcomingSchedule(studentId);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch trình sắp tới của sinh viên ID {}: ", studentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
