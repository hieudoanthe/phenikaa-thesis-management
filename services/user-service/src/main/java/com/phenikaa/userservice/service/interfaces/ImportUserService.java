package com.phenikaa.userservice.service.interfaces;

import com.phenikaa.userservice.dto.response.ImportResultResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ImportUserService {
    
    /**
     * Import sinh viên từ file CSV
     */
    ImportResultResponse importStudentsFromCSV(MultipartFile file, Integer periodId, Integer academicYearId);
    
    /**
     * Import giảng viên từ file CSV
     */
    ImportResultResponse importTeachersFromCSV(MultipartFile file);
    
    /**
     * Lấy danh sách sinh viên theo đợt đăng ký
     */
    List<Map<String, Object>> getStudentsByPeriod(Integer periodId);
    
    /**
     * Xóa sinh viên khỏi đợt đăng ký
     */
    boolean removeStudentFromPeriod(Integer studentId, Integer periodId);
}
