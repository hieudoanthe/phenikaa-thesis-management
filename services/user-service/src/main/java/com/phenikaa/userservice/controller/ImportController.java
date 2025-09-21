package com.phenikaa.userservice.controller;

import com.phenikaa.userservice.dto.response.ImportResultResponse;
import com.phenikaa.userservice.service.interfaces.ImportUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/user-service/admin")
@RequiredArgsConstructor
@Slf4j
public class ImportController {

    private final ImportUserService importUserService;

    /**
     * Import sinh viên từ file CSV
     */
    @PostMapping("/import-students")
    public ResponseEntity<?> importStudents(
            @RequestParam("file") MultipartFile file,
            @RequestParam("periodId") Integer periodId,
            @RequestParam("academicYearId") Integer academicYearId) {
        try {
            log.info("Nhận request import sinh viên từ CSV cho periodId: {}, academicYearId: {}", periodId, academicYearId);
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "File CSV không được để trống"));
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Chỉ chấp nhận file CSV"));
            }

            ImportResultResponse result = importUserService.importStudentsFromCSV(file, periodId, academicYearId);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi import sinh viên từ CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi import sinh viên: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách sinh viên theo đợt đăng ký
     */
    @GetMapping("/students/by-period/{periodId}")
    public ResponseEntity<?> getStudentsByPeriod(@PathVariable Integer periodId) {
        try {
            log.info("Lấy danh sách sinh viên theo periodId: {}", periodId);
            
            List<Map<String, Object>> students = importUserService.getStudentsByPeriod(periodId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", students,
                "message", "Lấy danh sách sinh viên thành công"
            ));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sinh viên theo period: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi lấy danh sách sinh viên: " + e.getMessage()));
        }
    }

    /**
     * Import giảng viên từ file CSV
     */
    @PostMapping("/import-teachers")
    public ResponseEntity<?> importTeachers(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Nhận request import giảng viên từ CSV");
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "File CSV không được để trống"));
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Chỉ chấp nhận file CSV"));
            }

            // Xử lý async để tránh timeout
            CompletableFuture<ImportResultResponse> future = CompletableFuture.supplyAsync(() -> {
                return importUserService.importTeachersFromCSV(file);
            });
            
            // Chờ kết quả với timeout 5 phút
            ImportResultResponse result = future.get(5, TimeUnit.MINUTES);
            
            return ResponseEntity.ok(result);
        } catch (TimeoutException e) {
            log.error("Import giảng viên timeout sau 5 phút");
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body(Map.of("success", false, "message", "Import timeout - vui lòng thử lại với file nhỏ hơn"));
        } catch (Exception e) {
            log.error("Lỗi khi import giảng viên từ CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi import giảng viên: " + e.getMessage()));
        }
    }

    /**
     * Xóa sinh viên khỏi đợt đăng ký
     */
    @DeleteMapping("/students/{studentId}/period/{periodId}")
    public ResponseEntity<?> removeStudentFromPeriod(
            @PathVariable Integer studentId,
            @PathVariable Integer periodId) {
        try {
            log.info("Xóa sinh viên {} khỏi period {}", studentId, periodId);
            
            boolean success = importUserService.removeStudentFromPeriod(studentId, periodId);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa sinh viên khỏi đợt đăng ký thành công"
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Không thể xóa sinh viên khỏi đợt đăng ký"));
            }
        } catch (Exception e) {
            log.error("Lỗi khi xóa sinh viên khỏi period: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi xóa sinh viên: " + e.getMessage()));
        }
    }
}
