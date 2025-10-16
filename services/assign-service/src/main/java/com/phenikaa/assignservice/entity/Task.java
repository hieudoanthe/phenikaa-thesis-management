package com.phenikaa.assignservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "task", schema = "HieuDT")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Integer taskId;

    @Column(name = "topic_id")
    private Integer topicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @Column(name = "assigned_to")
    private Integer assignedTo;

    @Column(name = "assigned_by")
    private Integer assignedBy;

    @Column(name = "task_name", nullable = false, columnDefinition = "nvarchar(255)")
    private String taskName;

    @Column(name = "description", columnDefinition = "nvarchar(255)")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "status")
    private Integer status;

    @Column(name = "progress")
    private Float progress;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "task_status")
    private Integer taskStatus;

}
