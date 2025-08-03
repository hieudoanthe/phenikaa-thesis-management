package com.phenikaa.academicservice.service.interfaces;

import com.phenikaa.dto.AcademicDto;

import java.util.List;

public interface AcademicService {
    AcademicDto getAcademicDto(Integer yearId, String yearName);
    List<AcademicDto> findByYearId(Integer yearId);
    List<AcademicDto> findAll();
}
