package com.phenikaa.academicservice.service.interfaces;

import com.phenikaa.dto.response.GetAcademicResponse;
import com.phenikaa.academicservice.entity.AcademicYear;

import java.util.List;

public interface AcademicService {
    GetAcademicResponse getAcademicDto(Integer yearId, String yearName);
    List<GetAcademicResponse> findByYearId(Integer yearId);
    List<GetAcademicResponse> findAll();
    
    // Lấy năm học đang active
    GetAcademicResponse getActiveAcademicYear();
    
    // Active một năm học
    GetAcademicResponse activateAcademicYear(Integer yearId);
    
    // Deactive tất cả năm học khác
    void deactivateOtherAcademicYears(Integer yearId);
}
