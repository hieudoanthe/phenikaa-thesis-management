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

    @Column(name = "total_score")
    private Float totalScore; // Tổng điểm (0-10)

    @Column(name = "comments", columnDefinition = "TEXT")
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

    // Method để tính tổng điểm
    public void calculateTotalScore() {
        if (evaluationType == EvaluationType.COMMITTEE) {
            // Hội đồng: có thêm điểm bảo vệ
            if (contentScore != null && presentationScore != null && 
                technicalScore != null && innovationScore != null && defenseScore != null) {
                this.totalScore = (contentScore + presentationScore + technicalScore + innovationScore + defenseScore) / 5;
            }
        } else {
            // GVHD và GVPB: không có điểm bảo vệ
            if (contentScore != null && presentationScore != null && 
                technicalScore != null && innovationScore != null) {
                this.totalScore = (contentScore + presentationScore + technicalScore + innovationScore) / 4;
            }
        }
    }
}