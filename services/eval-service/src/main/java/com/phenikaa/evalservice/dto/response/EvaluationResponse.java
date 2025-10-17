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
    
    // Các trường điểm mới cho từng vai trò
    // Hội đồng (COMMITTEE) - 6 tiêu chí
    private Float presentationClarityScore;
    private Float reviewerQaScore;
    private Float committeeQaScore;
    private Float attitudeScore;
    private Float contentImplementationScore;
    private Float relatedIssuesScore;
    
    // Giảng viên phản biện (REVIEWER) - 5 tiêu chí
    private Float formatScore;
    private Float contentQualityScore;
    private Float relatedIssuesReviewerScore;
    private Float practicalApplicationScore;
    private Float bonusScore;
    
    // Giảng viên hướng dẫn (SUPERVISOR) - 6 tiêu chí
    private Float studentAttitudeScore;
    private Float problemSolvingScore;
    private Float formatSupervisorScore;
    private Float contentImplementationSupervisorScore;
    private Float relatedIssuesSupervisorScore;
    private Float practicalApplicationSupervisorScore;
    
    // Thông tin bổ sung
    private String studentName;
    private String topicTitle;
    private String evaluatorName;
    private LocalDate defenseDate;
    private LocalDateTime defenseTime;
}
