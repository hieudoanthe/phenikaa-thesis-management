package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.dto.DefenseSessionDto;
import com.phenikaa.evalservice.dto.DefenseSessionExportDto;
import com.phenikaa.evalservice.entity.DefenseSession;
import com.phenikaa.evalservice.entity.StudentDefense;
import com.phenikaa.evalservice.exception.DefenseSessionValidationException;
import com.phenikaa.evalservice.service.DefenseSessionService;
import com.phenikaa.evalservice.service.StudentAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/eval-service/admin/sessions")
@RequiredArgsConstructor
@Slf4j
public class DefenseSessionController {

    private final DefenseSessionService defenseSessionService;
    private final StudentAssignmentService studentAssignmentService;

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
    public ResponseEntity<?> createSession(@RequestBody DefenseSessionDto sessionDto) {
        try {
            DefenseSessionDto createdSession = defenseSessionService.createSession(sessionDto);
            return ResponseEntity.ok(createdSession);
        } catch (DefenseSessionValidationException e) {
            log.error("Lỗi validation khi tạo buổi bảo vệ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi tạo buổi bảo vệ: ", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật buổi bảo vệ
     */
    @PutMapping("/{sessionId}")
    public ResponseEntity<?> updateSession(
            @PathVariable Integer sessionId,
            @RequestBody DefenseSessionDto sessionDto) {
        try {
            DefenseSessionDto updatedSession = defenseSessionService.updateSession(sessionId, sessionDto);
            return ResponseEntity.ok(updatedSession);
        } catch (DefenseSessionValidationException e) {
            log.error("Lỗi validation khi cập nhật buổi bảo vệ ID {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật buổi bảo vệ ID {}: ", sessionId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
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
     * Xuất dữ liệu buổi bảo vệ (thông tin + hội đồng)
     */
    @GetMapping("/{sessionId}/export")
    public ResponseEntity<DefenseSessionExportDto> exportSession(@PathVariable Integer sessionId) {
        try {
            DefenseSessionExportDto dto = defenseSessionService.exportSession(sessionId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Lỗi khi export buổi bảo vệ ID {}: ", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Xuất dữ liệu tất cả buổi bảo vệ
     */
    @GetMapping("/export/all")
    public ResponseEntity<List<DefenseSessionExportDto>> exportAllSessions() {
        try {
            List<DefenseSessionExportDto> list = defenseSessionService.exportAllSessions();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Lỗi khi export tất cả buổi bảo vệ: ", e);
            return ResponseEntity.internalServerError().build();
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

    // ========== STUDENT ASSIGNMENT ENDPOINTS ==========

    /**
     * Gán sinh viên vào buổi bảo vệ
     */
    @PostMapping("/{sessionId}/students")
    public ResponseEntity<Map<String, Object>> assignStudentToSession(
            @PathVariable Integer sessionId,
            @RequestBody Map<String, Object> request) {
        try {
            Integer studentId = (Integer) request.get("studentId");
            Integer topicId = (Integer) request.get("topicId");
            Integer supervisorId = (Integer) request.get("supervisorId");
            String studentName = (String) request.get("studentName");
            String studentMajor = (String) request.get("studentMajor");
            String topicTitle = (String) request.get("topicTitle");

            if (studentId == null || topicId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "studentId và topicId là bắt buộc"));
            }

            boolean success = studentAssignmentService.assignStudentToSession(
                sessionId, studentId, topicId, supervisorId, studentName, studentMajor, topicTitle);

            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Gán sinh viên thành công"));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể gán sinh viên vào buổi bảo vệ"));
            }
        } catch (Exception e) {
            log.error("Lỗi khi gán sinh viên vào buổi bảo vệ {}: ", sessionId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Hủy gán sinh viên khỏi buổi bảo vệ
     */
    @DeleteMapping("/{sessionId}/students/{studentId}")
    public ResponseEntity<Map<String, Object>> unassignStudentFromSession(
            @PathVariable Integer sessionId,
            @PathVariable Integer studentId) {
        try {
            boolean success = studentAssignmentService.unassignStudentFromSession(sessionId, studentId);

            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Hủy gán sinh viên thành công"));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể hủy gán sinh viên"));
            }
        } catch (Exception e) {
            log.error("Lỗi khi hủy gán sinh viên {} khỏi buổi bảo vệ {}: ", studentId, sessionId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách sinh viên đã được gán vào buổi bảo vệ
     */
    @GetMapping("/{sessionId}/students")
    public ResponseEntity<List<Map<String, Object>>> getAssignedStudents(@PathVariable Integer sessionId) {
        try {
            List<StudentDefense> assignments = studentAssignmentService.getAssignedStudents(sessionId);
            List<Map<String, Object>> result = assignments.stream()
                .map(assignment -> {
                    Map<String, Object> map = Map.of(
                        "studentId", assignment.getStudentId(),
                        "studentName", assignment.getStudentName(),
                        "studentMajor", assignment.getStudentMajor(),
                        "topicId", assignment.getTopicId(),
                        "topicTitle", assignment.getTopicTitle(),
                        "supervisorId", assignment.getSupervisorId(),
                        "defenseOrder", assignment.getDefenseOrder(),
                        "status", assignment.getStatus().toString()
                    );
                    return map;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sinh viên đã gán cho buổi bảo vệ {}: ", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy danh sách buổi bảo vệ có thể gán thêm sinh viên
     */
    @GetMapping("/available")
    public ResponseEntity<List<DefenseSessionDto>> getAvailableSessions() {
        try {
            List<DefenseSession> availableSessions = studentAssignmentService.getAvailableSessions();
            List<DefenseSessionDto> result = availableSessions.stream()
                .map(session -> DefenseSessionDto.builder()
                    .sessionId(session.getSessionId())
                    .sessionName(session.getSessionName())
                    .defenseDate(session.getDefenseDate())
                    .startTime(session.getStartTime())
                    .endTime(session.getEndTime())
                    .location(session.getLocation())
                    .maxStudents(session.getMaxStudents())
                    .status(session.getStatus())
                    .build())
                .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách buổi bảo vệ có sẵn: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
