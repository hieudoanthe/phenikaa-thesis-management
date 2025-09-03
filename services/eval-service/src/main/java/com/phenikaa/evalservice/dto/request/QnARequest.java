package com.phenikaa.evalservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QnARequest {
    
    @NotNull(message = "Topic ID không được để trống")
    private Integer topicId;

    @NotNull(message = "Student ID không được để trống")
    private Integer studentId;

    @NotNull(message = "Questioner ID không được để trống")
    private Integer questionerId;

    @NotNull(message = "Secretary ID không được để trống")
    private Integer secretaryId;

    @NotBlank(message = "Câu hỏi không được để trống")
    private String question;

    private String answer;
}
