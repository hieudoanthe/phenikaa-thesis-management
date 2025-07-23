package com.phenikaa.academicservice.controller;

import com.phenikaa.academicservice.service.interfaces.AcademicService;
import com.phenikaa.common.dto.AcademicDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BaseController {

    private final AcademicService academicService;

    @GetMapping("/academic")
    public AcademicDto getAcademicYear(@RequestParam Integer yearId, @RequestParam String yearName) {
        return academicService.getAcademicDto(yearId, yearName);
    }

    @GetMapping("/listAcademicYear")
    public List<AcademicDto> getAcademicYears() {
        return academicService.findAll();
    }
}
