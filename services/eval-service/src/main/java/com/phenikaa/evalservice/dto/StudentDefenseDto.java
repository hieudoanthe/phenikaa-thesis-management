package com.phenikaa.evalservice.dto;

import com.phenikaa.evalservice.entity.StudentDefense;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDefenseDto {

    private Integer studentDefenseId;
    private Integer sessionId;
    private Integer studentId;
    private Integer topicId;
    private Integer supervisorId;
    private String studentName;
    private String studentMajor;
    private String topicTitle;
    private Integer defenseOrder;
    private LocalDateTime defenseTime;
    private Integer durationMinutes;
    private StudentDefense.DefenseStatus status;
    private Double score;
    private String comments;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Thông tin bổ sung
    private String sessionName;
    private String supervisorName;
    private String studentEmail;

    // Convert từ Entity
    public static StudentDefenseDto fromEntity(StudentDefense entity) {
        return StudentDefenseDto.builder()
                .studentDefenseId(entity.getStudentDefenseId())
                .sessionId(entity.getDefenseSession() != null ? entity.getDefenseSession().getSessionId() : null)
                .studentId(entity.getStudentId())
                .topicId(entity.getTopicId())
                .supervisorId(entity.getSupervisorId())
                .studentName(entity.getStudentName())
                .studentMajor(entity.getStudentMajor())
                .topicTitle(entity.getTopicTitle())
                .defenseOrder(entity.getDefenseOrder())
                .defenseTime(entity.getDefenseTime())
                .durationMinutes(entity.getDurationMinutes())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
