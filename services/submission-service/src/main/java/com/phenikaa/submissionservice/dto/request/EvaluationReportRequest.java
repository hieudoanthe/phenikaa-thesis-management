package com.phenikaa.submissionservice.dto.request;

import com.phenikaa.submissionservice.dto.response.FeedbackResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationReportRequest {
    
    private Integer topicId;
    private Integer studentId;
    private String studentName;
    private String topicTitle;
    private String supervisorName;
    private String reviewerName;
    private String committeeMembers;
    private LocalDateTime defenseDate;
    private String defenseTime;
    private String defenseRoom;
    
    // Điểm số
    private Double supervisorScore;
    private Double reviewerScore;
    private Double committeeScore;
    private Double finalScore;
    
    // Chi tiết điểm số
    private SupervisorScoreDetails supervisorDetails;
    private ReviewerScoreDetails reviewerDetails;
    private CommitteeScoreDetails committeeDetails;
    
    // Thông tin báo cáo
    private String reportTitle;
    private String reportDescription;
    private String reportFilePath;
    private LocalDateTime reportSubmittedAt;
    
    // Phản hồi
    private List<FeedbackResponse> feedbacks;
    
    // Thông tin bổ sung
    private String academicYear;
    private String semester;
    private String department;
    private String major;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SupervisorScoreDetails {
        private Double studentAttitudeScore;
        private Double problemSolvingScore;
        private Double formatSupervisorScore;
        private Double contentImplementationSupervisorScore;
        private Double relatedIssuesSupervisorScore;
        private Double practicalApplicationSupervisorScore;
        private Double totalScore;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewerScoreDetails {
        private Double formatScore;
        private Double contentQualityScore;
        private Double relatedIssuesReviewerScore;
        private Double practicalApplicationScore;
        private Double bonusScore;
        private Double totalScore;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommitteeScoreDetails {
        private Double presentationClarityScore;
        private Double reviewerQaScore;
        private Double committeeQaScore;
        private Double attitudeScore;
        private Double contentImplementationScore;
        private Double relatedIssuesScore;
        private Double totalScore;
    }
}
