package com.phenikaa.academicservice.service.implement;

import com.phenikaa.academicservice.dto.CreateAcademicYearRequest;
import com.phenikaa.academicservice.dto.UpdateAcademicYearRequest;
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
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy năm học với ID: " + yearId));
        
        if (!year.canActivate()) {
            throw new IllegalArgumentException("Năm học này không thể được kích hoạt. Có thể đã quá ngày kết thúc.");
        }
        
        // Kiểm tra xem năm học này đã active chưa
        if (year.isActive()) {
            throw new IllegalArgumentException("Năm học này đã được kích hoạt rồi.");
        }
        
        // Deactive tất cả năm học khác trước khi active năm học này
        deactivateOtherAcademicYears(yearId);
        
        // Active năm học được chọn
        year.setStatus(AcademicYear.Status.ACTIVE.getValue());
        academicRepository.save(year);
        
        // Validate để đảm bảo chỉ có 1 năm học active
        validateSingleActiveAcademicYear();
        
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
    public GetAcademicResponse deactivateAcademicYear(Integer yearId) {
        AcademicYear year = academicRepository.findById(yearId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy năm học với ID: " + yearId));
        
        // Kiểm tra xem năm học này đã inactive chưa
        if (!year.isActive()) {
            throw new IllegalArgumentException("Năm học này đã được vô hiệu hóa rồi.");
        }
        
        // Deactivate năm học
        year.setStatus(AcademicYear.Status.INACTIVE.getValue());
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
    public GetAcademicResponse createAcademicYear(CreateAcademicYearRequest request) {
        // Validate input
        if (request.getYearName() == null || request.getYearName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên năm học không được để trống");
        }
        
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
        }
        
        // Tạo AcademicYear entity
        AcademicYear academicYear = new AcademicYear();
        
        // Generate yearId - có thể cần logic phức tạp hơn tùy theo yêu cầu
        Integer maxYearId = academicRepository.findAll().stream()
                .mapToInt(AcademicYear::getYearId)
                .max()
                .orElse(0);
        academicYear.setYearId(maxYearId + 1);
        
        academicYear.setYearName(request.getYearName().trim());
        academicYear.setStartDate(request.getStartDate());
        academicYear.setEndDate(request.getEndDate());
        
        // Set status - luôn tạo với trạng thái INACTIVE để tránh conflict
        // Nếu muốn active, phải gọi API activate riêng
        academicYear.setStatus(AcademicYear.Status.INACTIVE.getValue());
        
        // Save to database
        AcademicYear savedYear = academicRepository.save(academicYear);
        
        // Convert to response DTO
        GetAcademicResponse dto = new GetAcademicResponse();
        dto.setAcademicYearId(savedYear.getYearId());
        dto.setYearName(savedYear.getYearName());
        dto.setStartDate(savedYear.getStartDate());
        dto.setEndDate(savedYear.getEndDate());
        dto.setStatus(savedYear.getStatus());
        dto.setCreatedAt(savedYear.getCreatedAt());
        
        return dto;
    }

    @Override
    public GetAcademicResponse updateAcademicYear(Integer yearId, UpdateAcademicYearRequest request) {
        // Kiểm tra năm học có tồn tại không
        AcademicYear academicYear = academicRepository.findById(yearId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy năm học với ID: " + yearId));
        
        // Validate input
        if (request.getYearName() == null || request.getYearName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên năm học không được để trống");
        }
        
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
        }
        
        // Cập nhật thông tin năm học
        academicYear.setYearName(request.getYearName().trim());
        academicYear.setStartDate(request.getStartDate());
        academicYear.setEndDate(request.getEndDate());
        
        // Kiểm tra nếu năm học đang active và ngày kết thúc đã qua
        if (academicYear.isActive() && !academicYear.canActivate()) {
            throw new IllegalArgumentException("Không thể cập nhật năm học đang hoạt động đã quá ngày kết thúc");
        }
        
        // Save to database
        AcademicYear savedYear = academicRepository.save(academicYear);
        
        // Convert to response DTO
        GetAcademicResponse dto = new GetAcademicResponse();
        dto.setAcademicYearId(savedYear.getYearId());
        dto.setYearName(savedYear.getYearName());
        dto.setStartDate(savedYear.getStartDate());
        dto.setEndDate(savedYear.getEndDate());
        dto.setStatus(savedYear.getStatus());
        dto.setCreatedAt(savedYear.getCreatedAt());
        
        return dto;
    }

    @Override
    public void deleteAcademicYear(Integer yearId) {
        // Kiểm tra năm học có tồn tại không
        AcademicYear academicYear = academicRepository.findById(yearId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy năm học với ID: " + yearId));
        
        // Kiểm tra năm học có đang active không
        if (academicYear.isActive()) {
            throw new IllegalArgumentException("Không thể xóa năm học đang hoạt động. Vui lòng kích hoạt năm học khác trước.");
        }
        // Xóa năm học
        academicRepository.deleteById(yearId);
    }

    @Override
    public int deactivateOtherAcademicYears(Integer yearId) {
        List<AcademicYear> allYears = academicRepository.findAll();
        int deactivatedCount = 0;
        
        for (AcademicYear year : allYears) {
            if (!year.getYearId().equals(yearId) && year.isActive()) {
                year.setStatus(AcademicYear.Status.INACTIVE.getValue());
                academicRepository.save(year);
                deactivatedCount++;
            }
        }
        
        return deactivatedCount;
    }

    @Override
    public void validateSingleActiveAcademicYear() {
        List<AcademicYear> activeYears = academicRepository.findAll().stream()
                .filter(AcademicYear::isActive)
                .collect(Collectors.toList());
        
        if (activeYears.size() > 1) {
            // Nếu có nhiều hơn 1 năm học active, chỉ giữ lại năm học đầu tiên
            for (int i = 1; i < activeYears.size(); i++) {
                AcademicYear year = activeYears.get(i);
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
