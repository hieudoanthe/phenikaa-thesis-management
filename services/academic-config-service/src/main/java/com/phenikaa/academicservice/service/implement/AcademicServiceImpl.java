package com.phenikaa.academicservice.service.implement;

import com.phenikaa.academicservice.entity.AcademicYear;
import com.phenikaa.academicservice.repository.AcademicRepository;
import com.phenikaa.academicservice.service.interfaces.AcademicService;
import com.phenikaa.common.dto.AcademicDto;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AcademicServiceImpl implements AcademicService {
    private final AcademicRepository academicRepository;

    @Override
    public AcademicDto getAcademicDto(Integer yearId, String yearName) {
        AcademicYear entity = academicRepository.findByYearIdAndYearName(yearId, yearName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học kỳ"));

        AcademicDto dto = new AcademicDto();
        dto.setAcademicYearId(entity.getYearId());
        dto.setYearName(entity.getYearName());
        return dto;
    }

    @Override
    public List<AcademicDto> findByYearId(Integer yearId) {
        return academicRepository.findByYearId(yearId)
                .stream()
                .map(entity -> new AcademicDto(entity.getYearId(), entity.getYearName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AcademicDto> findAll() {
        return academicRepository.findAll()
                .stream()
                .map(e -> new AcademicDto(e.getYearId(), e.getYearName()))
                .collect(Collectors.toList());
    }

}
