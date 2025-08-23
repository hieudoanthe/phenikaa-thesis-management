package com.phenikaa.thesisservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "project_topic", schema = "HieuDT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"suggestedTopics","registers"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProjectTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Integer topicId;

    @Column(name = "topic_code")
    private String topicCode;

    @Column(name = "title", columnDefinition = "nvarchar(255)")
    private String title;

    @Column(name = "description", columnDefinition = "nvarchar(255)")
    private String description;

    @Column(name = "objectives", columnDefinition = "nvarchar(255)")
    private String objectives;

    @Column(name = "methodology", columnDefinition = "nvarchar(255)")
    private String methodology;

    @Column(name = "expected_outcome", columnDefinition = "nvarchar(255)")
    private String expectedOutcome;

    @Column(name = "supervisor_id")
    private Integer supervisorId;

    @Column(name = "academic_year_id")
    private Integer academicYearId;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    public enum DifficultyLevel {
        EASY, MEDIUM, HARD;
    }

    @Column(name = "topic_status")
    @Enumerated(EnumType.STRING)
    private TopicStatus topicStatus;

    public enum TopicStatus {
        ACTIVE, INACTIVE, ARCHIVED, DELETED
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "approval_status")
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    public enum ApprovalStatus {
        PENDING, AVAILABLE, APPROVED, REJECTED
    }

    @OneToMany(mappedBy = "projectTopic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<SuggestedTopic> suggestedTopics;

    @OneToMany(mappedBy = "projectTopic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Register> registers;

    @PrePersist
    protected void onCreate() {
        if (maxStudents == null) {
            maxStudents = 15;
        }
        if (topicStatus == null) {
            topicStatus = TopicStatus.ACTIVE;
        }
        if (approvalStatus == null) {
            approvalStatus = ApprovalStatus.AVAILABLE;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}