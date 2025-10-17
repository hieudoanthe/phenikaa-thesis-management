package com.phenikaa.thesisservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "suggested_topic_feedback", schema = "HieuDT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestedTopicFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "topic_title", columnDefinition = "nvarchar(500)")
    private String topicTitle;

    @Column(name = "feedback", length = 16)
    @Enumerated(EnumType.STRING)
    private FeedbackType feedback; // LIKE, NEUTRAL, DISLIKE

    public enum FeedbackType { LIKE, NEUTRAL, DISLIKE }

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (feedback == null) feedback = FeedbackType.NEUTRAL;
    }
}


