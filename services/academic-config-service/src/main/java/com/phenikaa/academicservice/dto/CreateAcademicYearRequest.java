package com.phenikaa.academicservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAcademicYearRequest {
    private String yearName;
    private LocalDate startDate;
    private LocalDate endDate;
}
