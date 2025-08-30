package com.phenikaa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAcademicResponse {
    private Integer academicYearId;
    private String yearName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private LocalDateTime createdAt;
}
