package com.phenikaa.evalservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_defense", schema = "HieuDT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"defenseSession"})
public class StudentDefense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_defense_id")
    private Integer studentDefenseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private DefenseSession defenseSession;

    @Column(name = "student_id", nullable = false)
    private Integer studentId; // Reference to UserService

    @Column(name = "topic_id", nullable = false)
    private Integer topicId; // Reference to ThesisService

    @Column(name = "supervisor_id")
    private Integer supervisorId; // Giảng viên hướng dẫn

    @Column(name = "student_name")
    private String studentName; // Tên sinh viên

    @Column(name = "student_major")
    private String studentMajor; // Chuyên ngành của sinh viên

    @Column(name = "topic_title")
    private String topicTitle; // Tên đề tài

    @Column(name = "defense_order")
    private Integer defenseOrder; // Thứ tự bảo vệ trong buổi

    @Column(name = "defense_time")
    private LocalDateTime defenseTime; // Thời gian bảo vệ cụ thể

    @Column(name = "duration_minutes")
    private Integer durationMinutes; // Thời gian bảo vệ (phút)

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DefenseStatus status;

    public enum DefenseStatus {
        SCHEDULED,    // Đã lên lịch
        IN_PROGRESS,  // Đang bảo vệ
        COMPLETED,    // Đã hoàn thành
        CANCELLED,    // Đã hủy
        NO_SHOW       // Không tham gia
    }

    @Column(name = "score")
    private Double score; // Điểm bảo vệ

    @Column(name = "comments")
    private String comments; // Nhận xét của hội đồng

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = DefenseStatus.SCHEDULED;
        }
        if (durationMinutes == null) {
            durationMinutes = 30; // Mặc định 30 phút
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
