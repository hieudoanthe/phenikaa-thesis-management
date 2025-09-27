package com.phenikaa.academicservice.controller;

import com.phenikaa.academicservice.service.interfaces.AcademicService;
import com.phenikaa.dto.response.GetAcademicResponse;
import lombok.RequiredArgsConstructor;
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
}
