package com.phenikaa.assignservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAssignmentRequest {
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer priority;
    private Integer status;
}
