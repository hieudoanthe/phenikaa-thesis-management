package com.phenikaa.evalservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_evaluation", schema = "HieuDT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEvaluation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evaluation_id")
    private Integer evaluationId;

    @Column(name = "topic_id", nullable = false)
    private Integer topicId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "evaluator_id", nullable = false)
    private Integer evaluatorId;

    @Column(name = "evaluation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EvaluationType evaluationType;

    // Điểm số theo từng tiêu chí (0-10)
    @Column(name = "content_score")
    private Float contentScore; // Điểm nội dung

    @Column(name = "presentation_score")
    private Float presentationScore; // Điểm thuyết trình

    @Column(name = "technical_score")
    private Float technicalScore; // Điểm kỹ thuật

    @Column(name = "innovation_score")
    private Float innovationScore; // Điểm sáng tạo

    @Column(name = "defense_score")
    private Float defenseScore; // Điểm bảo vệ (chỉ cho hội đồng)

    // Các trường điểm mới cho từng vai trò
    // Hội đồng (COMMITTEE) - 6 tiêu chí
    @Column(name = "presentation_clarity_score")
    private Float presentationClarityScore; // Trình bày nội dung (0-0.5)

    @Column(name = "reviewer_qa_score")
    private Float reviewerQaScore; // Trả lời câu hỏi GVPB (0-1.5)

    @Column(name = "committee_qa_score")
    private Float committeeQaScore; // Trả lời câu hỏi hội đồng (0-1.5)

    @Column(name = "attitude_score")
    private Float attitudeScore; // Tinh thần, thái độ (0-1)

    @Column(name = "content_implementation_score")
    private Float contentImplementationScore; // Thực hiện nội dung đề tài (0-4.5)

    @Column(name = "related_issues_score")
    private Float relatedIssuesScore; // Mối liên hệ vấn đề liên quan (0-1)

    // Giảng viên phản biện (REVIEWER) - 5 tiêu chí
    @Column(name = "format_score")
    private Float formatScore; // Hình thức trình bày (0-1.5)

    @Column(name = "content_quality_score")
    private Float contentQualityScore; // Thực hiện nội dung đề tài (0-4)

    @Column(name = "related_issues_reviewer_score")
    private Float relatedIssuesReviewerScore; // Mối liên hệ vấn đề liên quan (0-2)

    @Column(name = "practical_application_score")
    private Float practicalApplicationScore; // Tính ứng dụng thực tiễn (0-2)

    @Column(name = "bonus_score")
    private Float bonusScore; // Điểm thưởng (0-0.5)

    // Giảng viên hướng dẫn (SUPERVISOR) - 6 tiêu chí
    @Column(name = "student_attitude_score")
    private Float studentAttitudeScore; // Ý thức, thái độ sinh viên (0-1)

    @Column(name = "problem_solving_score")
    private Float problemSolvingScore; // Khả năng xử lý vấn đề (0-1)

    @Column(name = "format_supervisor_score")
    private Float formatSupervisorScore; // Hình thức trình bày (0-1.5)

    @Column(name = "content_implementation_supervisor_score")
    private Float contentImplementationSupervisorScore; // Thực hiện nội dung đề tài (0-4.5)

    @Column(name = "related_issues_supervisor_score")
    private Float relatedIssuesSupervisorScore; // Mối liên hệ vấn đề liên quan (0-1)

    @Column(name = "practical_application_supervisor_score")
    private Float practicalApplicationSupervisorScore; // Tính ứng dụng thực tiễn (0-1)

    @Column(name = "total_score")
    private Float totalScore; // Tổng điểm (0-10)

    @Column(name = "comments", columnDefinition = "nvarchar(2000)")
    private String comments;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "evaluation_status")
    @Enumerated(EnumType.STRING)
    private EvaluationStatus evaluationStatus;

    // Enum cho loại đánh giá
    public enum EvaluationType {
        SUPERVISOR,     // Giảng viên hướng dẫn (25%)
        REVIEWER,       // Giảng viên phản biện (50%)
        COMMITTEE       // Hội đồng chấm (25%)
    }

    // Enum cho trạng thái đánh giá
    public enum EvaluationStatus {
        PENDING,        // Chờ chấm
        IN_PROGRESS,    // Đang chấm
        COMPLETED,      // Hoàn thành
        CANCELLED       // Hủy bỏ
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (evaluationStatus == null) {
            evaluationStatus = EvaluationStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Method để tính tổng điểm theo vai trò
    public void calculateTotalScore() {
        if (evaluationType == EvaluationType.COMMITTEE) {
            // Hội đồng: 6 tiêu chí (tổng 10 điểm)
            if (presentationClarityScore != null && reviewerQaScore != null && 
                committeeQaScore != null && attitudeScore != null && 
                contentImplementationScore != null && relatedIssuesScore != null) {
                this.totalScore = presentationClarityScore + reviewerQaScore + 
                                committeeQaScore + attitudeScore + 
                                contentImplementationScore + relatedIssuesScore;
            }
        } else if (evaluationType == EvaluationType.REVIEWER) {
            // Giảng viên phản biện: 5 tiêu chí (tổng 10 điểm)
            if (formatScore != null && contentQualityScore != null && 
                relatedIssuesReviewerScore != null && practicalApplicationScore != null && 
                bonusScore != null) {
                this.totalScore = formatScore + contentQualityScore + 
                                relatedIssuesReviewerScore + practicalApplicationScore + bonusScore;
            }
        } else if (evaluationType == EvaluationType.SUPERVISOR) {
            // Giảng viên hướng dẫn: 6 tiêu chí (tổng 10 điểm)
            if (studentAttitudeScore != null && problemSolvingScore != null && 
                formatSupervisorScore != null && contentImplementationSupervisorScore != null && 
                relatedIssuesSupervisorScore != null && practicalApplicationSupervisorScore != null) {
                this.totalScore = studentAttitudeScore + problemSolvingScore + 
                                formatSupervisorScore + contentImplementationSupervisorScore + 
                                relatedIssuesSupervisorScore + practicalApplicationSupervisorScore;
            }
        }
    }
}