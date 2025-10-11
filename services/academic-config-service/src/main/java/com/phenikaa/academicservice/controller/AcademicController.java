package com.phenikaa.academicservice.controller;

import com.phenikaa.academicservice.dto.CreateAcademicYearRequest;
import com.phenikaa.academicservice.dto.UpdateAcademicYearRequest;
import com.phenikaa.academicservice.service.interfaces.AcademicService;
import com.phenikaa.dto.response.GetAcademicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/academic-config-service")
@RequiredArgsConstructor
public class AcademicController {

    private final AcademicService academicService;

    @GetMapping("/academic")
    public GetAcademicResponse getAcademicYear(@RequestParam Integer yearId, @RequestParam String yearName) {
        return academicService.getAcademicDto(yearId, yearName);
    }

    @GetMapping("/list-academic-year")
    public List<GetAcademicResponse> getAcademicYears() {
        return academicService.findAll();
    }

    @GetMapping("/active")
    public GetAcademicResponse getActiveAcademicYear() {
        return academicService.getActiveAcademicYear();
    }

    @PostMapping("/{yearId}/activate")
    public GetAcademicResponse activateAcademicYear(@PathVariable Integer yearId) {
        return academicService.activateAcademicYear(yearId);
    }

    @PutMapping("/{yearId}/deactivate")
    public GetAcademicResponse deactivateAcademicYear(@PathVariable Integer yearId) {
        return academicService.deactivateAcademicYear(yearId);
    }

    @PostMapping("/create")
    public GetAcademicResponse createAcademicYear(@RequestBody CreateAcademicYearRequest request) {
        return academicService.createAcademicYear(request);
    }

    @PutMapping("/{yearId}")
    public GetAcademicResponse updateAcademicYear(@PathVariable Integer yearId, @RequestBody UpdateAcademicYearRequest request) {
        return academicService.updateAcademicYear(yearId, request);
    }

    @DeleteMapping("/{yearId}")
    public ResponseEntity<String> deleteAcademicYear(@PathVariable Integer yearId) {
        try {
            academicService.deleteAcademicYear(yearId);
            return ResponseEntity.ok("Xóa năm học thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
