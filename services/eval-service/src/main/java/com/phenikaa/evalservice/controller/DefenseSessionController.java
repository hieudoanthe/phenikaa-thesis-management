package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.dto.DefenseSessionDto;
import com.phenikaa.evalservice.entity.DefenseSession;
import com.phenikaa.evalservice.service.DefenseSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/eval-service/admin/sessions")
@RequiredArgsConstructor
@Slf4j
public class DefenseSessionController {

    private final DefenseSessionService defenseSessionService;

    /**
     * Lấy tất cả buổi bảo vệ
     */
    @GetMapping
    public ResponseEntity<List<DefenseSessionDto>> getAllSessions() {
        try {
            List<DefenseSessionDto> sessions = defenseSessionService.getAllSessions();
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Lỗi khi lấy tất cả buổi bảo vệ: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Tạo buổi bảo vệ mới
     */
    @PostMapping
    public ResponseEntity<DefenseSessionDto> createSession(@RequestBody DefenseSessionDto sessionDto) {
        try {
            DefenseSessionDto createdSession = defenseSessionService.createSession(sessionDto);
            return ResponseEntity.ok(createdSession);
        } catch (Exception e) {
            log.error("Lỗi khi tạo buổi bảo vệ: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cập nhật buổi bảo vệ
     */
    @PutMapping("/{sessionId}")
    public ResponseEntity<DefenseSessionDto> updateSession(
            @PathVariable Integer sessionId,
            @RequestBody DefenseSessionDto sessionDto) {
        try {
            DefenseSessionDto updatedSession = defenseSessionService.updateSession(sessionId, sessionDto);
            return ResponseEntity.ok(updatedSession);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật buổi bảo vệ ID {}: ", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy buổi bảo vệ theo khoảng thời gian
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<DefenseSessionDto>> getSessionsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        try {
            List<DefenseSessionDto> sessions = defenseSessionService.getSessionsByDateRange(startDate, endDate);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Lỗi khi lấy buổi bảo vệ theo khoảng thời gian: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy buổi bảo vệ theo ngày
     */
    @GetMapping("/date")
    public ResponseEntity<List<DefenseSessionDto>> getSessionsByDate(@RequestParam LocalDate date) {
        try {
            List<DefenseSessionDto> sessions = defenseSessionService.getSessionsByDate(date);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Lỗi khi lấy buổi bảo vệ theo ngày {}: ", date, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy tất cả buổi bảo vệ theo lịch
     */
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<DefenseSessionDto>> getSessionsBySchedule(@PathVariable Integer scheduleId) {
        try {
            List<DefenseSessionDto> sessions = defenseSessionService.getSessionsBySchedule(scheduleId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Lỗi khi lấy buổi bảo vệ theo lịch {}: ", scheduleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy buổi bảo vệ theo trạng thái
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DefenseSessionDto>> getSessionsByStatus(@PathVariable String status) {
        try {
            DefenseSession.SessionStatus sessionStatus = DefenseSession.SessionStatus.valueOf(status.toUpperCase());
            List<DefenseSessionDto> sessions = defenseSessionService.getSessionsByStatus(sessionStatus);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Lỗi khi lấy buổi bảo vệ theo trạng thái {}: ", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy các buổi bảo vệ có thể thêm sinh viên
     */
    @GetMapping("/available")
    public ResponseEntity<List<DefenseSessionDto>> getAvailableSessions() {
        try {
            List<DefenseSessionDto> sessions = defenseSessionService.getAvailableSessions();
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Lỗi khi lấy buổi bảo vệ có sẵn: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy buổi bảo vệ theo địa điểm
     */
    @GetMapping("/location")
    public ResponseEntity<List<DefenseSessionDto>> getSessionsByLocation(@RequestParam String location) {
        try {
            List<DefenseSessionDto> sessions = defenseSessionService.getSessionsByLocation(location);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Lỗi khi lấy buổi bảo vệ theo địa điểm {}: ", location, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy buổi bảo vệ theo ID
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<DefenseSessionDto> getSessionById(@PathVariable Integer sessionId) {
        try {
            DefenseSessionDto session = defenseSessionService.getSessionById(sessionId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("Lỗi khi lấy buổi bảo vệ ID {}: ", sessionId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cập nhật trạng thái buổi bảo vệ
     */
    @PutMapping("/{sessionId}/status")
    public ResponseEntity<Void> updateSessionStatus(
            @PathVariable Integer sessionId,
            @RequestParam String status) {
        try {
            DefenseSession.SessionStatus sessionStatus = DefenseSession.SessionStatus.valueOf(status.toUpperCase());
            defenseSessionService.updateSessionStatus(sessionId, sessionStatus);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật trạng thái buổi bảo vệ ID {}: ", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Xóa buổi bảo vệ
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Integer sessionId) {
        try {
            defenseSessionService.deleteSession(sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Lỗi khi xóa buổi bảo vệ ID {}: ", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
