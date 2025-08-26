package com.phenikaa.assignservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {
    private Integer assignedTo;
    private String taskName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer priority;
    private Integer status;
    private Float progress;
}


