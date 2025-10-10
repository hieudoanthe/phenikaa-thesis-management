package com.phenikaa.evalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionPreviewDto {
    private String sessionId;
    private String sessionName;
    private String location;
    private LocalDateTime defenseDate;
    private LocalDateTime startTime;
    private Integer maxStudents;
    private Boolean virtualSession;
    private List<StudentPreviewDto> students;
}


