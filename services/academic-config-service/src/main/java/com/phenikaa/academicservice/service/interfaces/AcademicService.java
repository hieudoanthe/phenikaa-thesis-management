package com.phenikaa.academicservice.service.interfaces;

import com.phenikaa.academicservice.dto.CreateAcademicYearRequest;
import com.phenikaa.academicservice.dto.UpdateAcademicYearRequest;
import com.phenikaa.dto.response.GetAcademicResponse;

import java.util.List;
import java.util.Map;

public interface AcademicService {
    GetAcademicResponse getAcademicDto(Integer yearId, String yearName);
    List<GetAcademicResponse> findByYearId(Integer yearId);
    List<GetAcademicResponse> findAll();
    
    // Lấy năm học đang active
    GetAcademicResponse getActiveAcademicYear();
    
    // Active một năm học
    GetAcademicResponse activateAcademicYear(Integer yearId);
    
    // Deactivate một năm học
    GetAcademicResponse deactivateAcademicYear(Integer yearId);
    
    // Tạo năm học mới
    GetAcademicResponse createAcademicYear(CreateAcademicYearRequest request);
    
    // Cập nhật năm học
    GetAcademicResponse updateAcademicYear(Integer yearId, UpdateAcademicYearRequest request);
    
    // Xóa năm học
    void deleteAcademicYear(Integer yearId);
    
    // Deactive tất cả năm học khác
    int deactivateOtherAcademicYears(Integer yearId);
    
    // Validate rằng chỉ có 1 năm học active
    void validateSingleActiveAcademicYear();
    
    // Statistics methods
    List<Map<String, Object>> getAllAcademicYears();
    Map<String, Object> getActiveAcademicYearInfo();
    Long getAcademicYearCount();
    Map<String, Object> getAcademicYearById(Integer yearId);
}
