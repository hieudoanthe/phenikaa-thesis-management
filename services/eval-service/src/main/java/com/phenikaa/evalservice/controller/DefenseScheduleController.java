package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.dto.DefenseScheduleDto;
import com.phenikaa.evalservice.service.StudentAssignmentService;
import com.phenikaa.evalservice.service.DefenseScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eval-service/admin")
@RequiredArgsConstructor
@Slf4j
public class DefenseScheduleController {

    private final DefenseScheduleService defenseScheduleService;

    /**
     * Lấy danh sách lịch bảo vệ
     */
    @GetMapping("/schedules")
    public ResponseEntity<List<DefenseScheduleDto>> getAllSchedules() {
        try {
            List<DefenseScheduleDto> schedules = defenseScheduleService.getAllSchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách lịch bảo vệ: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy lịch bảo vệ theo ID
     */
    @GetMapping("/schedules/{scheduleId}")
    public ResponseEntity<DefenseScheduleDto> getScheduleById(@PathVariable Integer scheduleId) {
        try {
            DefenseScheduleDto schedule = defenseScheduleService.getScheduleById(scheduleId);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch bảo vệ ID {}: ", scheduleId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Tạo lịch bảo vệ mới
     */
    @PostMapping("/schedules")
    public ResponseEntity<DefenseScheduleDto> createSchedule(@RequestBody DefenseScheduleDto scheduleDto) {
        try {
            DefenseScheduleDto createdSchedule = defenseScheduleService.createSchedule(scheduleDto);
            return ResponseEntity.ok(createdSchedule);
        } catch (Exception e) {
            log.error("Lỗi khi tạo lịch bảo vệ: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cập nhật lịch bảo vệ
     */
    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<DefenseScheduleDto> updateSchedule(
            @PathVariable Integer scheduleId,
            @RequestBody DefenseScheduleDto scheduleDto) {
        try {
            DefenseScheduleDto updatedSchedule = defenseScheduleService.updateSchedule(scheduleId, scheduleDto);
            return ResponseEntity.ok(updatedSchedule);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật lịch bảo vệ ID {}: ", scheduleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Xóa lịch bảo vệ
     */
    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Integer scheduleId) {
        try {
            defenseScheduleService.deleteSchedule(scheduleId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Lỗi khi xóa lịch bảo vệ ID {}: ", scheduleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Kích hoạt lịch bảo vệ
     */
    @PostMapping("/schedules/{scheduleId}/activate")
    public ResponseEntity<Void> activateSchedule(@PathVariable Integer scheduleId) {
        try {
            defenseScheduleService.activateSchedule(scheduleId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Lỗi khi kích hoạt lịch bảo vệ ID {}: ", scheduleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Hủy kích hoạt lịch bảo vệ
     */
    @PostMapping("/schedules/{scheduleId}/deactivate")
    public ResponseEntity<Void> deactivateSchedule(@PathVariable Integer scheduleId) {
        try {
            defenseScheduleService.deactivateSchedule(scheduleId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Lỗi khi hủy kích hoạt lịch bảo vệ ID {}: ", scheduleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
