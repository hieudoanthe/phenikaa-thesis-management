package com.phenikaa.evalservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "defense_committee", schema = "HieuDT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"defenseSession"})
public class DefenseCommittee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "committee_id")
    private Integer committeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private DefenseSession defenseSession;

    @Column(name = "lecturer_id", nullable = false)
    private Integer lecturerId; // Reference to UserService

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private CommitteeRole role;

    public enum CommitteeRole {
        CHAIRMAN,      // Chủ tịch hội đồng
        SECRETARY,     // Thư ký
        MEMBER,        // Thành viên
        SUPERVISOR,    // Giảng viên hướng dẫn
        REVIEWER       // Giảng viên phản biện
    }

    @Column(name = "specialization")
    private String specialization; // Chuyên ngành của giảng viên

    @Column(name = "department")
    private String department; // Khoa của giảng viên

    @Column(name = "academic_title")
    private String academicTitle; // Học hàm, học vị

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CommitteeStatus status;

    public enum CommitteeStatus {
        INVITED,       // Đã mời
        CONFIRMED,     // Đã xác nhận
        DECLINED,      // Từ chối
        CANCELLED      // Đã hủy
    }

    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

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
            status = CommitteeStatus.CONFIRMED;
        }
        if (role == null) {
            role = CommitteeRole.MEMBER;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
