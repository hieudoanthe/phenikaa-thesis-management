package com.phenikaa.evalservice.dto.response;

import com.phenikaa.evalservice.entity.ProjectEvaluation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponse {
    
    private Integer evaluationId;
    private Integer topicId;
    private Integer studentId;
    private Integer evaluatorId;
    private ProjectEvaluation.EvaluationType evaluationType;
    private Float contentScore;
    private Float presentationScore;
    private Float technicalScore;
    private Float innovationScore;
    private Float defenseScore;
    private Float totalScore;
    private String comments;
    private LocalDateTime evaluatedAt;
    private ProjectEvaluation.EvaluationStatus evaluationStatus;
    
    // Thông tin bổ sung
    private String studentName;
    private String topicTitle;
    private String evaluatorName;
    private LocalDate defenseDate;
    private LocalDateTime defenseTime;
}
