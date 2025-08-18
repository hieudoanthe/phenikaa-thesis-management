package com.phenikaa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAcademicResponse {
    private Integer academicYearId;
    private String yearName;
}
