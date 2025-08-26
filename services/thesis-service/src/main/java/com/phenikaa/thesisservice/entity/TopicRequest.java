package com.phenikaa.thesisservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "topic_request", schema = "HieuDT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TopicRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "keywords")
    private String keywords;

    @Column(name = "description")
    private String description;

    @Column(name = "field_of_study")
    private String fieldOfStudy;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "topic_request_status")
    @Enumerated(EnumType.STRING)
    private TopicRequestStatus topicRequestStatus;

    public enum TopicRequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "priority")
    private Integer priority;

    @OneToMany(mappedBy = "topicRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AiSuggestedTopic> aiSuggestedTopics;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}