package com.phenikaa.evalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPreviewDto {
    private Integer studentId;
    private String studentName;
    private String topicTitle;
    private Integer reviewerId; 
    private String reviewerName;
    private String reviewerSpecialization;
}


