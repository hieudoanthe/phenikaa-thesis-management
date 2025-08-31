package com.phenikaa.evalservice.dto;

import com.phenikaa.evalservice.entity.DefenseSchedule;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseScheduleDto {

    private Integer scheduleId;
    private String scheduleName;
    private Integer academicYearId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String description;
    private DefenseSchedule.ScheduleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer createdBy;
    
    // Thông tin bổ sung
    private String academicYearName;
    private Integer totalSessions;
    private Integer totalStudents;
    private List<DefenseSessionDto> sessions;

    // Convert từ Entity
    public static DefenseScheduleDto fromEntity(DefenseSchedule entity) {
        return DefenseScheduleDto.builder()
                .scheduleId(entity.getScheduleId())
                .scheduleName(entity.getScheduleName())
                .academicYearId(entity.getAcademicYearId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .location(entity.getLocation())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}
