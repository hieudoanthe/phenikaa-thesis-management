package com.phenikaa.evalservice.dto;

import com.phenikaa.evalservice.entity.DefenseSession;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseSessionDto {

    private Integer sessionId;
    private String sessionName;
    private Integer scheduleId;
    private LocalDate defenseDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Integer maxStudents;
    private DefenseSession.SessionStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Thông tin bổ sung
    private String scheduleName;
    private Integer currentStudents;
    private List<DefenseCommitteeDto> committees;
    private List<StudentDefenseDto> students;
    
    // Dữ liệu từ frontend để tạo hội đồng
    private List<Integer> committeeMembers; // List of lecturer IDs
    private List<Integer> reviewerMembers; // List of reviewer lecturer IDs

    // Convert từ Entity
    public static DefenseSessionDto fromEntity(DefenseSession entity) {
        return DefenseSessionDto.builder()
                .sessionId(entity.getSessionId())
                .sessionName(entity.getSessionName())
                .scheduleId(entity.getDefenseSchedule() != null ? entity.getDefenseSchedule().getScheduleId() : null)
                .defenseDate(entity.getDefenseDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .location(entity.getLocation())
                .maxStudents(entity.getMaxStudents())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
