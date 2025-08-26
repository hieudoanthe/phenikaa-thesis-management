package com.phenikaa.assignservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    private Integer assignedTo; // tuỳ chọn, mặc định = assignedTo của assignment
    private String taskName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer priority;
    private Integer status; // tuỳ chọn
    private Float progress; // tuỳ chọn
}


