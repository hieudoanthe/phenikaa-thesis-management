package com.phenikaa.assignservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {
    private Integer assignmentId;
    private Integer topicId;
    private Integer assignedTo;
    private Integer assignedBy;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer priority;
    private Integer status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    private List<TaskResponse> tasks;
}
