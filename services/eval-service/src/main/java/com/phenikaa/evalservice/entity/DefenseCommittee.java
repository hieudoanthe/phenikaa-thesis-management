package com.phenikaa.evalservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "defense_committee", schema = "HieuDT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefenseCommittee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "committee_id")
    private Integer committeeId;

    @Column(name = "schedule_id", nullable = false)
    private Integer scheduleId;

    @Column(name = "teacher_id", nullable = false)
    private Integer teacherId;

    @Column(name = "topic_id", nullable = false)
    private Integer topicId;

    @Column(name = "role")
    private Integer role;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }
}
