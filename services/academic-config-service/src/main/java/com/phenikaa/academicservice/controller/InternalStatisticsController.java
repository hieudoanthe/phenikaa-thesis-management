package com.phenikaa.academicservice.controller;

import com.phenikaa.academicservice.service.interfaces.AcademicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/academic")
@RequiredArgsConstructor
@Slf4j
public class InternalStatisticsController {
    
    private final AcademicService academicService;
    
    @GetMapping("/get-academic-years")
    public List<Map<String, Object>> getAcademicYears() {
        log.info("Getting all academic years");
        return academicService.getAllAcademicYears();
    }
    
    @GetMapping("/get-active-academic-year")
    public Map<String, Object> getActiveAcademicYear() {
        log.info("Getting active academic year");
        return academicService.getActiveAcademicYearInfo();
    }
    
    @GetMapping("/get-academic-year-count")
    public Long getAcademicYearCount() {
        log.info("Getting academic year count");
        return academicService.getAcademicYearCount();
    }
    
    @GetMapping("/get-academic-year-by-id")
    public Map<String, Object> getAcademicYearById(@RequestParam Integer yearId) {
        log.info("Getting academic year by id: {}", yearId);
        return academicService.getAcademicYearById(yearId);
    }
}
