package com.phenikaa.academicservice.service.interfaces;

import com.phenikaa.dto.response.GetAcademicResponse;

import java.util.List;

public interface AcademicService {
    GetAcademicResponse getAcademicDto(Integer yearId, String yearName);
    List<GetAcademicResponse> findByYearId(Integer yearId);
    List<GetAcademicResponse> findAll();
}
