package com.phenikaa.academicservice.service.implement;

import com.phenikaa.academicservice.entity.AcademicYear;
import com.phenikaa.academicservice.repository.AcademicRepository;
import com.phenikaa.academicservice.service.interfaces.AcademicService;
import com.phenikaa.dto.response.GetAcademicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AcademicServiceImpl implements AcademicService {
    private final AcademicRepository academicRepository;

    @Override
    public GetAcademicResponse getAcademicDto(Integer yearId, String yearName) {
        AcademicYear entity = academicRepository.findByYearIdAndYearName(yearId, yearName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học kỳ"));

        GetAcademicResponse dto = new GetAcademicResponse();
        dto.setAcademicYearId(entity.getYearId());
        dto.setYearName(entity.getYearName());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        
        return dto;
    }

    @Override
    public List<GetAcademicResponse> findByYearId(Integer yearId) {
        return academicRepository.findByYearId(yearId)
                .stream()
                .map(entity -> {
                    GetAcademicResponse dto = new GetAcademicResponse();
                    dto.setAcademicYearId(entity.getYearId());
                    dto.setYearName(entity.getYearName());
                    dto.setStartDate(entity.getStartDate());
                    dto.setEndDate(entity.getEndDate());
                    dto.setStatus(entity.getStatus());
                    dto.setCreatedAt(entity.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<GetAcademicResponse> findAll() {
        return academicRepository.findAll()
                .stream()
                .map(entity -> {
                    GetAcademicResponse dto = new GetAcademicResponse();
                    dto.setAcademicYearId(entity.getYearId());
                    dto.setYearName(entity.getYearName());
                    dto.setStartDate(entity.getStartDate());
                    dto.setStatus(entity.getStatus());
                    dto.setEndDate(entity.getEndDate());
                    dto.setCreatedAt(entity.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public GetAcademicResponse getActiveAcademicYear() {
        AcademicYear activeYear = academicRepository.findByStatus(AcademicYear.Status.ACTIVE.getValue())
                .orElse(null);
        
        if (activeYear == null) {
            return null;
        }
        
        GetAcademicResponse dto = new GetAcademicResponse();
        dto.setAcademicYearId(activeYear.getYearId());
        dto.setYearName(activeYear.getYearName());
        dto.setStartDate(activeYear.getStartDate());
        dto.setEndDate(activeYear.getEndDate());
        dto.setStatus(activeYear.getStatus());
        dto.setCreatedAt(activeYear.getCreatedAt());
        return dto;
    }

    @Override
    public GetAcademicResponse activateAcademicYear(Integer yearId) {
        AcademicYear year = academicRepository.findById(yearId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy năm học với ID: " + yearId));
        
        if (!year.canActivate()) {
            throw new RuntimeException("Năm học này không thể được kích hoạt");
        }
        
        // Deactive tất cả năm học khác
        deactivateOtherAcademicYears(yearId);
        
        // Active năm học được chọn
        year.setStatus(AcademicYear.Status.ACTIVE.getValue());
        academicRepository.save(year);
        
        GetAcademicResponse dto = new GetAcademicResponse();
        dto.setAcademicYearId(year.getYearId());
        dto.setYearName(year.getYearName());
        dto.setStartDate(year.getStartDate());
        dto.setEndDate(year.getEndDate());
        dto.setStatus(year.getStatus());
        dto.setCreatedAt(year.getCreatedAt());
        return dto;
    }

    @Override
    public void deactivateOtherAcademicYears(Integer yearId) {
        List<AcademicYear> allYears = academicRepository.findAll();
        for (AcademicYear year : allYears) {
            if (!year.getYearId().equals(yearId)) {
                year.setStatus(AcademicYear.Status.INACTIVE.getValue());
                academicRepository.save(year);
            }
        }
    }

    // Statistics methods implementation
    @Override
    public List<Map<String, Object>> getAllAcademicYears() {
        return academicRepository.findAll().stream()
                .map(year -> {
                    Map<String, Object> yearMap = new HashMap<>();
                    yearMap.put("yearId", year.getYearId());
                    yearMap.put("yearName", year.getYearName());
                    yearMap.put("startDate", year.getStartDate());
                    yearMap.put("endDate", year.getEndDate());
                    yearMap.put("status", year.getStatus());
                    yearMap.put("createdAt", year.getCreatedAt());
                    return yearMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getActiveAcademicYearInfo() {
        AcademicYear activeYear = academicRepository.findByStatus(AcademicYear.Status.ACTIVE.getValue())
                .orElse(null);
        
        if (activeYear == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> yearMap = new HashMap<>();
        yearMap.put("yearId", activeYear.getYearId());
        yearMap.put("yearName", activeYear.getYearName());
        yearMap.put("startDate", activeYear.getStartDate());
        yearMap.put("endDate", activeYear.getEndDate());
        yearMap.put("status", activeYear.getStatus());
        yearMap.put("createdAt", activeYear.getCreatedAt());
        return yearMap;
    }

    @Override
    public Long getAcademicYearCount() {
        return academicRepository.count();
    }

    @Override
    public Map<String, Object> getAcademicYearById(Integer yearId) {
        AcademicYear year = academicRepository.findById(yearId).orElse(null);
        
        if (year == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> yearMap = new HashMap<>();
        yearMap.put("yearId", year.getYearId());
        yearMap.put("yearName", year.getYearName());
        yearMap.put("startDate", year.getStartDate());
        yearMap.put("endDate", year.getEndDate());
        yearMap.put("status", year.getStatus());
        yearMap.put("createdAt", year.getCreatedAt());
        return yearMap;
    }
}
