package com.phenikaa.evalservice.dto;

import com.phenikaa.evalservice.entity.DefenseCommittee;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseCommitteeDto {

    private Integer committeeId;
    private Integer sessionId;
    private Integer lecturerId;
    private DefenseCommittee.CommitteeRole role;
    private String specialization;
    private String department;
    private String academicTitle;
    private DefenseCommittee.CommitteeStatus status;
    private LocalDateTime invitedAt;
    private LocalDateTime respondedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Thông tin bổ sung
    private String lecturerName;
    private String lecturerEmail;
    private String sessionName;

    // Convert từ Entity
    public static DefenseCommitteeDto fromEntity(DefenseCommittee entity) {
        return DefenseCommitteeDto.builder()
                .committeeId(entity.getCommitteeId())
                .sessionId(entity.getDefenseSession() != null ? entity.getDefenseSession().getSessionId() : null)
                .lecturerId(entity.getLecturerId())
                .role(entity.getRole())
                .specialization(entity.getSpecialization())
                .department(entity.getDepartment())
                .academicTitle(entity.getAcademicTitle())
                .status(entity.getStatus())
                .invitedAt(entity.getInvitedAt())
                .respondedAt(entity.getRespondedAt())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
