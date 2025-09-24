package com.phenikaa.evalservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "defense_schedule", schema = "HieuDT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"defenseSessions"})
public class DefenseSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer scheduleId;

    @Column(name = "schedule_name", nullable = false, columnDefinition = "nvarchar(255)")
    private String scheduleName; // Ví dụ: "Lịch bảo vệ Học kỳ 1 - 2025"

    @Column(name = "academic_year_id")
    private Integer academicYearId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "location", columnDefinition = "nvarchar(255)")
    private String location; // Địa điểm bảo vệ

    @Column(name = "description", columnDefinition = "nvarchar(255)")
    private String description;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;

    public enum ScheduleStatus {
        PLANNING,    // Đang lập kế hoạch
        ACTIVE,      // Đang diễn ra
        COMPLETED,   // Đã hoàn thành
        CANCELLED    // Đã hủy
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @OneToMany(mappedBy = "defenseSchedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DefenseSession> defenseSessions;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ScheduleStatus.PLANNING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
