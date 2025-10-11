package com.phenikaa.userservice.service.interfaces;

import com.phenikaa.userservice.dto.response.ImportResultResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ImportUserService {
    ImportResultResponse importStudentsFromCSV(MultipartFile file, Integer periodId, Integer academicYearId);
    ImportResultResponse importTeachersFromCSV(MultipartFile file);
    List<Map<String, Object>> getStudentsByPeriod(Integer periodId);
    boolean removeStudentFromPeriod(Integer studentId, Integer periodId);
}
