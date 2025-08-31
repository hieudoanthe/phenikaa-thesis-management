package com.phenikaa.thesisservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "register", schema = "HieuDT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Register {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "register_id")
    private Integer registerId;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "register_type")
    @Enumerated(EnumType.STRING)
    private RegisterType registerType;

    public enum RegisterType {
        INDIVIDUAL,
        GROUP
    }

    @Column(name = "registered_at")
    private Instant registeredAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approved_by")
    private Integer approvedBy;

    @Column(name = "register_status")
    @Enumerated(EnumType.STRING)
    private RegisterStatus registerStatus;

    public enum RegisterStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Column(name = "registration_period_id")
    private Integer registrationPeriodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private ProjectTopic projectTopic;

    @PrePersist
    protected void onCreate() {
        registeredAt = Instant.now();
        if (registerType == null) {
            registerType = RegisterType.INDIVIDUAL;
        }
        if (registerStatus == null) {
            registerStatus = RegisterStatus.PENDING;
        }

    }
}