package com.phenikaa.evalservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "supervisor_summary", schema = "HieuDT")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupervisorSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer topicId;

    private Integer supervisorId;

    @Column(columnDefinition = "nvarchar(2000)")
    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}


