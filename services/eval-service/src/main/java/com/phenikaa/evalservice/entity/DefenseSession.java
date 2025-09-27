package com.phenikaa.evalservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "defense_session", schema = "HieuDT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"defenseSchedule", "defenseCommittees", "studentDefenses"})
public class DefenseSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Integer sessionId;

    @Column(name = "session_name", columnDefinition = "nvarchar(255)")
    private String sessionName; // Ví dụ: "Buổi 1 - Sáng thứ 2"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private DefenseSchedule defenseSchedule;

    @Column(name = "defense_date", nullable = false)
    private LocalDate defenseDate;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "location", columnDefinition = "nvarchar(255)")
    private String location;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    public enum SessionStatus {
        PLANNING,    // Đang lập kế hoạch
        SCHEDULED,   // Đã lên lịch
        IN_PROGRESS, // Đang diễn ra
        COMPLETED,   // Đã hoàn thành
        CANCELLED    // Đã hủy
    }

    @Column(name = "notes", columnDefinition = "nvarchar(255)")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "defenseSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DefenseCommittee> defenseCommittees;

    @OneToMany(mappedBy = "defenseSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StudentDefense> studentDefenses;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = SessionStatus.PLANNING;
        }
        if (maxStudents == null) {
            maxStudents = 5;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
