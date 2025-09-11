package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.dto.response.GetStudentPeriodResponse;
import com.phenikaa.thesisservice.entity.RegistrationPeriod;
import com.phenikaa.thesisservice.service.interfaces.RegistrationPeriodService;
import com.phenikaa.thesisservice.service.interfaces.StudentPeriodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/thesis-service/student-period")
@RequiredArgsConstructor
@Slf4j
public class StudentPeriodController {

    private final StudentPeriodService studentPeriodService;
    private final RegistrationPeriodService registrationPeriodService;

    /**
     * Lấy danh sách sinh viên đã đăng ký đề tài theo đợt đăng ký
     * @param periodId ID của đợt đăng ký
     * @return Danh sách sinh viên đã đăng ký
     */
    @GetMapping("/registered/{periodId}")
    public ResponseEntity<List<GetStudentPeriodResponse>> getRegisteredStudentsByPeriod(
            @PathVariable Integer periodId) {
        try {
            log.info("API được gọi: Lấy sinh viên đã đăng ký theo đợt {}", periodId);
            List<GetStudentPeriodResponse> students = studentPeriodService.getStudentsByPeriod(periodId);
            log.info("Tìm thấy {} sinh viên đã đăng ký", students.size());
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sinh viên đã đăng ký theo đợt {}: ", periodId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy danh sách sinh viên đã đề xuất đề tài theo đợt đăng ký
     * @param periodId ID của đợt đăng ký
     * @return Danh sách sinh viên đã đề xuất
     */
    @GetMapping("/suggested/{periodId}")
    public ResponseEntity<List<GetStudentPeriodResponse>> getSuggestedStudentsByPeriod(
            @PathVariable Integer periodId) {
        try {
            log.info("API được gọi: Lấy sinh viên đã đề xuất theo đợt {}", periodId);
            List<GetStudentPeriodResponse> students = studentPeriodService.getSuggestedStudentsByPeriod(periodId);
            log.info("Tìm thấy {} sinh viên đã đề xuất", students.size());
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sinh viên đã đề xuất theo đợt {}: ", periodId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy danh sách tất cả sinh viên (đăng ký + đề xuất) theo đợt đăng ký
     * @param periodId ID của đợt đăng ký
     * @return Danh sách tất cả sinh viên
     */
    @GetMapping("/all/{periodId}")
    public ResponseEntity<List<GetStudentPeriodResponse>> getAllStudentsByPeriod(
            @PathVariable Integer periodId) {
        try {
            log.info("API được gọi: Lấy tất cả sinh viên theo đợt {}", periodId);
            List<GetStudentPeriodResponse> students = studentPeriodService.getAllStudentsByPeriod(periodId);
            log.info("Tìm thấy tổng cộng {} sinh viên", students.size());
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách tất cả sinh viên theo đợt {}: ", periodId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy danh sách sinh viên theo đợt đăng ký (alias cho /all/{periodId})
     * @param periodId ID của đợt đăng ký
     * @return Danh sách tất cả sinh viên
     */
    @GetMapping("/{periodId}")
    public ResponseEntity<List<GetStudentPeriodResponse>> getStudentsByPeriod(
            @PathVariable Integer periodId) {
        return getAllStudentsByPeriod(periodId);
    }

    // Public cho sinh viên: lấy tất cả đợt ACTIVE hiện tại
    @GetMapping("/active")
    public ResponseEntity<List<RegistrationPeriod>> getActivePeriodsForStudents() {
        List<RegistrationPeriod> periods = registrationPeriodService.getAllActivePeriods();
        return ResponseEntity.ok(periods);
    }
}
