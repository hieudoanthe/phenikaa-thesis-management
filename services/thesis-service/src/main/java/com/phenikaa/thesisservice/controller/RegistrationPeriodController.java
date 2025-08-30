package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.entity.RegistrationPeriod;
import com.phenikaa.thesisservice.entity.LecturerCapacity;
import com.phenikaa.thesisservice.service.interfaces.RegistrationPeriodService;
import com.phenikaa.thesisservice.repository.LecturerCapacityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/thesis-service/admin")
@RequiredArgsConstructor
public class RegistrationPeriodController {

    private final RegistrationPeriodService registrationPeriodService;
    private final LecturerCapacityRepository lecturerCapacityRepository;

    @PostMapping
    public ResponseEntity<RegistrationPeriod> createPeriod(@RequestBody RegistrationPeriod period) {
        System.out.println("Received create period request: " + period); // Debug log
        System.out.println("Period ID before save: " + period.getPeriodId()); // Debug log
        
        RegistrationPeriod savedPeriod = registrationPeriodService.createPeriod(period);
        
        System.out.println("Saved period: " + savedPeriod); // Debug log
        System.out.println("Saved period ID: " + savedPeriod.getPeriodId()); // Debug log
        
        return ResponseEntity.ok(savedPeriod);
    }

    @GetMapping("/current")
    public ResponseEntity<RegistrationPeriod> getCurrentPeriod() {
        return ResponseEntity.ok(registrationPeriodService.getCurrentActivePeriod());
    }

    @GetMapping
    public ResponseEntity<List<RegistrationPeriod>> getAllPeriods() {
        return ResponseEntity.ok(registrationPeriodService.getAllPeriods());
    }

    @GetMapping("/academic-year/{academicYearId}")
    public ResponseEntity<List<RegistrationPeriod>> getPeriodsByAcademicYear(@PathVariable Integer academicYearId) {
        return ResponseEntity.ok(registrationPeriodService.getPeriodsByAcademicYear(academicYearId));
    }

    @GetMapping("/academic-year")
    public ResponseEntity<List<RegistrationPeriod>> getPeriodsByAcademicYearOptional(@RequestParam(required = false) Integer academicYearId) {
        return ResponseEntity.ok(registrationPeriodService.getPeriodsByAcademicYear(academicYearId));
    }

    @PostMapping("/{periodId}/start")
    public ResponseEntity<Void> startPeriod(@PathVariable Integer periodId) {
        registrationPeriodService.startPeriod(periodId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{periodId}/close")
    public ResponseEntity<Void> closePeriod(@PathVariable Integer periodId) {
        registrationPeriodService.closePeriod(periodId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{periodId}/check-auto-close")
    public ResponseEntity<Void> checkAndAutoClosePeriod(@PathVariable Integer periodId) {
        registrationPeriodService.checkAndAutoClosePeriod(periodId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auto-close-expired")
    public ResponseEntity<Void> autoCloseExpiredPeriods() {
        registrationPeriodService.autoCloseExpiredPeriods();
        return ResponseEntity.ok().build();
    }

    // ===== LECTURER CAPACITY ENDPOINTS =====
    
    @GetMapping("/lecturer-capacity/{lecturerId}/{periodId}")
    public ResponseEntity<LecturerCapacity> getLecturerCapacity(
            @PathVariable Integer lecturerId,
            @PathVariable Integer periodId) {
        Optional<LecturerCapacity> capacity = lecturerCapacityRepository
                .findByLecturerIdAndRegistrationPeriodId(lecturerId, periodId);
        
        if (capacity.isPresent()) {
            return ResponseEntity.ok(capacity.get());
        } else {
            // Nếu không có capacity, tạo mới với giá trị mặc định từ period
            RegistrationPeriod period = registrationPeriodService.getPeriodById(periodId);
            if (period != null) {
                LecturerCapacity newCapacity = LecturerCapacity.builder()
                        .lecturerId(lecturerId)
                        .registrationPeriodId(periodId)
                        .maxStudents(period.getMaxStudentsPerLecturer())
                        .currentStudents(0)
                        .build();
                
                LecturerCapacity savedCapacity = lecturerCapacityRepository.save(newCapacity);
                return ResponseEntity.ok(savedCapacity);
            } else {
                return ResponseEntity.notFound().build();
            }
        }
    }

    @GetMapping("/lecturer-capacity/all")
    public ResponseEntity<List<LecturerCapacity>> getAllLecturerCapacities() {
        List<LecturerCapacity> capacities = lecturerCapacityRepository.findAll();
        return ResponseEntity.ok(capacities);
    }

    @GetMapping("/lecturer-capacity/period/{periodId}")
    public ResponseEntity<List<LecturerCapacity>> getLecturerCapacitiesByPeriod(@PathVariable Integer periodId) {
        List<LecturerCapacity> capacities = lecturerCapacityRepository.findByRegistrationPeriodId(periodId);
        return ResponseEntity.ok(capacities);
    }

    @GetMapping("/lecturer-capacity/lecturer/{lecturerId}")
    public ResponseEntity<List<LecturerCapacity>> getLecturerCapacitiesByLecturer(@PathVariable Integer lecturerId) {
        List<LecturerCapacity> capacities = lecturerCapacityRepository.findByLecturerId(lecturerId);
        return ResponseEntity.ok(capacities);
    }
}
