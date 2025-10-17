package com.phenikaa.evalservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmAutoAssignRequest {
    private Integer scheduleId;
    private List<SessionAssignmentDto> assignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionAssignmentDto {
        private String sessionId;
        private String sessionName;
        private String location;
        private LocalDateTime defenseDate;
        private List<StudentAssignDto> students;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAssignDto {
        private Integer studentId;
        private Integer topicId;
        private Integer supervisorId; 
        private Integer reviewerId;
        private String studentName;
        private String specialization;
        private String topicTitle;
    }
}


