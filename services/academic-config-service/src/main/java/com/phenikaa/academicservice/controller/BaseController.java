package com.phenikaa.academicservice.controller;

import com.phenikaa.academicservice.service.interfaces.AcademicService;
import com.phenikaa.dto.AcademicDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/academic-config")
@RequiredArgsConstructor
public class BaseController {

    private final AcademicService academicService;

    @GetMapping("/academic")
    public AcademicDto getAcademicYear(@RequestParam Integer yearId, @RequestParam String yearName) {
        return academicService.getAcademicDto(yearId, yearName);
    }

    @GetMapping("/list-academic-year")
    public List<AcademicDto> getAcademicYears() {
        return academicService.findAll();
    }
}
