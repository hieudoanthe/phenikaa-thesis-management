package com.phenikaa.evalservice.dto;

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
        private String sessionId; // nếu null hoặc preview-* thì tạo mới
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
        private Integer reviewerId;
        private String topicTitle;
    }
}


