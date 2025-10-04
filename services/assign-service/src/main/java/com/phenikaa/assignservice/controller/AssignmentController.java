package com.phenikaa.assignservice.controller;

import com.phenikaa.assignservice.dto.request.CreateAssignmentRequest;
import com.phenikaa.assignservice.dto.request.UpdateAssignmentRequest;
import com.phenikaa.assignservice.dto.request.CreateTaskRequest;
import com.phenikaa.assignservice.dto.request.UpdateTaskRequest;
import com.phenikaa.assignservice.dto.response.AssignmentResponse;
import com.phenikaa.assignservice.service.AssignmentService;
import com.phenikaa.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assign-service")
@RequiredArgsConstructor
@Slf4j
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final JwtUtil jwtUtil;

    /**
     * Tạo assignment mới
     */
    @PostMapping
    public ResponseEntity<?> createAssignment(
            @RequestBody CreateAssignmentRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            Integer userId = jwtUtil.extractUserId(token);
            log.info("Nhận request tạo assignment mới từ user: {}", userId);
            
            AssignmentResponse response = assignmentService.createAssignment(request, userId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Lỗi khi tạo assignment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể tạo assignment: " + e.getMessage());
        }
    }

    /**
     * Tạo task cho một assignment
     */
    @PostMapping("/{assignmentId}/tasks")
    public ResponseEntity<?> createTask(
            @PathVariable Integer assignmentId,
            @RequestBody CreateTaskRequest request) {
        try {
            log.info("Nhận request tạo task cho assignment: {}", assignmentId);
            var response = assignmentService.createTask(assignmentId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Lỗi khi tạo task: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể tạo task: " + e.getMessage());
        }
    }

    /**
     * Cập nhật task
     */
    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable Integer taskId,
            @RequestBody UpdateTaskRequest request) {
        try {
            log.info("Nhận request cập nhật task: {}", taskId);
            var response = assignmentService.updateTask(taskId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật task: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể cập nhật task: " + e.getMessage());
        }
    }

    /**
     * Xoá task
     */
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer taskId) {
        try {
            log.info("Nhận request xoá task: {}", taskId);
            boolean deleted = assignmentService.deleteTask(taskId);
            if (deleted) return ResponseEntity.ok("Đã xoá task thành công");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy task để xoá");
        } catch (Exception e) {
            log.error("Lỗi khi xoá task: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể xoá task: " + e.getMessage());
        }
    }

    /**
     * Cập nhật assignment
     */
    @PutMapping("/{assignmentId}")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Integer assignmentId,
            @RequestBody UpdateAssignmentRequest request) {
        try {
            log.info("Nhận request cập nhật assignment với ID: {}", assignmentId);
            
            AssignmentResponse response = assignmentService.updateAssignment(assignmentId, request);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật assignment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể cập nhật assignment: " + e.getMessage());
        }
    }

    /**
     * Lấy assignment theo ID
     */
    @GetMapping("/{assignmentId}")
    public ResponseEntity<?> getAssignmentById(@PathVariable Integer assignmentId) {
        try {
            log.info("Nhận request lấy assignment với ID: {}", assignmentId);
            
            AssignmentResponse response = assignmentService.getAssignmentById(assignmentId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi lấy assignment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy assignment: " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả assignments theo topicId
     */
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<?> getAssignmentsByTopicId(@PathVariable Integer topicId) {
        try {
            log.info("Nhận request lấy assignments theo topicId: {}", topicId);
            
            List<AssignmentResponse> response = assignmentService.getAssignmentsByTopicId(topicId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi lấy assignments theo topicId: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể lấy assignments: " + e.getMessage());
        }
    }

    /**
     * Lấy assignments theo người được phân công
     */
    @GetMapping("/assigned-to/{assignedTo}")
    public ResponseEntity<?> getAssignmentsByAssignedTo(@PathVariable Integer assignedTo) {
        try {
            log.info("Nhận request lấy assignments theo assignedTo: {}", assignedTo);
            
            List<AssignmentResponse> response = assignmentService.getAssignmentsByAssignedTo(assignedTo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi lấy assignments theo assignedTo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể lấy assignments: " + e.getMessage());
        }
    }

    /**
     * Lấy assignments theo người phân công
     */
    @GetMapping("/assigned-by/{assignedBy}")
    public ResponseEntity<?> getAssignmentsByAssignedBy(@PathVariable Integer assignedBy) {
        try {
            log.info("Nhận request lấy assignments theo assignedBy: {}", assignedBy);
            
            List<AssignmentResponse> response = assignmentService.getAssignmentsByAssignedBy(assignedBy);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi lấy assignments theo assignedBy: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể lấy assignments: " + e.getMessage());
        }
    }

    /**
     * Lấy deadlines của sinh viên dạng lịch trình
     */
    @GetMapping("/student/{studentId}/deadlines")
    public ResponseEntity<?> getStudentDeadlinesSchedule(@PathVariable Integer studentId) {
        try {
            log.info("Nhận request lấy lịch deadlines cho sinh viên: {}", studentId);
            
            List<AssignmentResponse> assignments = assignmentService.getAssignmentsByAssignedTo(studentId);
            
            // Chuyển đổi thành format phù hợp cho schedule
            List<Map<String, Object>> scheduleItems = assignments.stream()
                    .filter(assignment -> assignment.getDueDate() != null) // Chỉ lấy assignments có deadline
                    .map(assignment -> {
                        Map<String, Object> scheduleItem = new HashMap<>();
                        scheduleItem.put("assignmentId", assignment.getAssignmentId());
                        scheduleItem.put("eventType", "deadline");
                        scheduleItem.put("title", assignment.getTitle());
                        scheduleItem.put("description", assignment.getDescription());
                        scheduleItem.put("dueDate", assignment.getDueDate().toString());
                        scheduleItem.put("dueTime", "23:59"); // Default deadline time
                        scheduleItem.put("priority", assignment.getPriority());
                        scheduleItem.put("status", getDeadlineStatus(assignment.getDueDate()));
                        scheduleItem.put("location", "Online"); // Deadlines thường online
                        return scheduleItem;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of("success", true, "data", scheduleItems));
        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch deadlines cho sinh viên {}: {}", studentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể lấy lịch deadlines: " + e.getMessage());
        }
    }

    /**
     * Xác định trạng thái deadline
     */
    private String getDeadlineStatus(LocalDate dueDate) {
        LocalDate today = LocalDate.now();
        if (dueDate.isBefore(today)) {
            return "completed"; // Đã qua hạn
        } else if (dueDate.isBefore(today.plusDays(3))) {
            return "urgent"; // Có hạn trong 3 ngày
        } else {
            return "upcoming"; // Còn xa
        }
    }

    /**
     * Xóa assignment
     */
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Integer assignmentId) {
        try {
            log.info("Nhận request xóa assignment với ID: {}", assignmentId);
            
            boolean deleted = assignmentService.deleteAssignment(assignmentId);
            
            if (deleted) {
                return ResponseEntity.ok("Đã xóa assignment thành công");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy assignment để xóa");
            }
        } catch (Exception e) {
            log.error("Lỗi khi xóa assignment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể xóa assignment: " + e.getMessage());
        }
    }

    /**
     * Cập nhật trạng thái assignment
     */
    @PatchMapping("/{assignmentId}/status")
    public ResponseEntity<?> updateAssignmentStatus(
            @PathVariable Integer assignmentId,
            @RequestParam Integer status) {
        try {
            log.info("Nhận request cập nhật trạng thái assignment với ID: {} thành status: {}", assignmentId, status);
            
            AssignmentResponse response = assignmentService.updateAssignmentStatus(assignmentId, status);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật trạng thái assignment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể cập nhật trạng thái assignment: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Assignment Service is running!");
    }
}
