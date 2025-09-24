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
}
