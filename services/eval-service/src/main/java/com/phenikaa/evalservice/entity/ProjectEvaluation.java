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

    @Column(name = "evaluator_id", nullable = false)
    private Integer evaluatorId;

    @Column(name = "score")
    private Float score;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "evaluation_type")
    private Integer evaluationType;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "evaluation_status")
    private Integer evaluationStatus;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
