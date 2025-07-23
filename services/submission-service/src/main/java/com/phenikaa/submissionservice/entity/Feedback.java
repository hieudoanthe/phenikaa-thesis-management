package com.phenikaa.submissionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback", schema = "HieuDT")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private ReportSubmission submission;

    @Column(name = "reviewer_id")
    private Integer reviewerId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "score")
    private Float score;

    @Column(name = "feedback_type")
    private Integer feedbackType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_approved")
    private Boolean isApproved;
}
