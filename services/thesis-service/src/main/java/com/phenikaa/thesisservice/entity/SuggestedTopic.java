package com.phenikaa.thesisservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "suggested_topic", schema = "HieuDT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SuggestedTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suggested_id")
    private Integer suggestedId;

    @Column(name = "suggested_by")
    private Integer suggestedBy;

    @Column(name = "suggested_for")
    private Integer suggestedFor;

    @Column(name = "reason", columnDefinition = "nvarchar(255)")
    private String reason;

    @Column(name = "suggestion_status")
    @Enumerated(EnumType.STRING)
    private SuggestionStatus suggestionStatus;

    public enum SuggestionStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "approved_by")
    private Integer approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private ProjectTopic projectTopic;

    // Thêm field mới để liên kết với đợt đăng ký
    @Column(name = "registration_period_id")
    private Integer registrationPeriodId;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}