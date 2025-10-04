package com.phenikaa.submissionservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionStatusResponse {
    private Integer userId;
    private String username;
    
    // Thesis milestones completion status
    private Boolean softCopySubmitted;      // submissionType = 2 (Báo cáo cuối kì - PDF)
    private Boolean hardCopySubmitted;     // submissionType = 3 (Bản cứng)
    private Boolean defenseCompleted;      // Có tham gia bảo vệ thành công
    private Boolean finalCopySubmitted;    // submissionType = 4 (Bìa đỏ)
    
    // Progress calculation
    private Integer progressPercentage;    // 0-100% tổng tiến độ
    private Integer completedMilestones;  // Số milestones đã hoàn thành
    private Integer totalMilestones;      // Tổng số milestones (4)
    
    // Additional info
    private LocalDateTime lastSubmissionDate;
    private Integer lastSubmissionType;
    private String lastSubmissionTypeDescription;
    private Integer lastSubmittedAssignmentId;
    
    // Milestone details
    private List<MilestoneDetail> milestones;
    
    // Inner class cho milestone detail
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneDetail {
        private String id;
        private String name;
        private Integer weight;
        private Boolean completed;
        private LocalDateTime completedAt;
        private String description;
    }
}
