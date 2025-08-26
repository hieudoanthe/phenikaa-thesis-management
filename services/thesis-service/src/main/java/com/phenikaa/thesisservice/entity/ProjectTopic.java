package com.phenikaa.thesisservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
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
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

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
            maxStudents = 4;
        }
        if (topicStatus == null) {
            topicStatus = TopicStatus.ACTIVE;
        }
        if (approvalStatus == null) {
            approvalStatus = ApprovalStatus.AVAILABLE;
        }
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Kiểm tra xem đề tài có thể nhận thêm sinh viên không
     * @return true nếu còn chỗ, false nếu đã đủ
     */
    public boolean canAcceptMoreStudents() {
        return maxStudents > 0;
    }

    /**
     * Kiểm tra xem đề tài có thể được approve không
     * @return true nếu có thể approve, false nếu đã đủ sinh viên
     */
    public boolean canBeApproved() {
        return canAcceptMoreStudents() && approvalStatus != ApprovalStatus.APPROVED;
    }

    /**
     * Giảm số lượng sinh viên có thể nhận khi approve đề tài
     * @return true nếu giảm thành công, false nếu không thể giảm
     */
    public boolean decreaseMaxStudents() {
        if (canAcceptMoreStudents()) {
            maxStudents--;
            return true;
        }
        return false;
    }

    /**
     * Lấy số lượng sinh viên còn lại có thể nhận
     * @return số lượng sinh viên còn lại
     */
    public Integer getRemainingStudentSlots() {
        return Math.max(0, maxStudents);
    }

    /**
     * Lấy số lượng sinh viên đã được nhận
     * @return số lượng sinh viên đã được nhận
     */
    public Integer getAcceptedStudentsCount() {
        return 4 - maxStudents;
    }

    /**
     * Kiểm tra xem đề tài có đủ sinh viên chưa
     * @return true nếu đã đủ, false nếu còn thiếu
     */
    public boolean isFull() {
        return maxStudents <= 0;
    }

    /**
     * Cập nhật trạng thái đề tài dựa trên số lượng sinh viên
     */
    public void updateTopicStatusBasedOnStudents() {
        if (isFull()) {
            // Nếu đã đủ sinh viên, chuyển sang trạng thái INACTIVE
            this.topicStatus = TopicStatus.INACTIVE;
            this.approvalStatus = ApprovalStatus.APPROVED;
        } else if (maxStudents < 4) {
            // Nếu đã có sinh viên nhưng chưa đủ, giữ nguyên trạng thái
            this.approvalStatus = ApprovalStatus.APPROVED;
        }
    }
}