package com.phenikaa.academicservice.service.interfaces;

import com.phenikaa.academicservice.entity.AcademicYear;
import com.phenikaa.common.dto.AcademicDto;

import java.time.LocalDate;
import java.util.List;

public interface AcademicService {
    AcademicDto getAcademicDto(Integer yearId, String yearName);
    List<AcademicDto> findByYearId(Integer yearId);
    List<AcademicDto> findAll();
}
