package com.phenikaa.assignservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Integer taskId;
    private Integer topicId;
    private Integer assignmentId;
    private Integer assignedTo;
    private Integer assignedBy;
    private String taskName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer priority;
    private Integer status;
    private Float progress;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer taskStatus;
}
