package com.phenikaa.thesisservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "ai_suggested_topic", schema = "HieuDT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AiSuggestedTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suggestion_id")
    private Integer suggestionId;

    @Column(name = "request_id")
    private Integer requestId;

    @Column(name = "topic_title")
    private String topicTitle;

    @Column(name = "description", columnDefinition = "NVARCHAR(255)")
    private String description;

    @Column(name = "objectives", columnDefinition = "NVARCHAR(255)")
    private String objectives;

    @Column(name = "methodology", columnDefinition = "NVARCHAR(255)")
    private String methodology;

    @Column(name = "relevance_score")
    private Float relevanceScore;

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
    private Integer approvedBy; // Reference to UserService

    // Relationship với Topic Request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", insertable = false, updatable = false)
    private TopicRequest topicRequest;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}