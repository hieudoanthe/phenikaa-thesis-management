package com.phenikaa.profileservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "student_profile", schema = "HieuDT")
public class StudentProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "student_code", unique = true)
    private String studentCode;

    @Column(name = "major")
    private String major;

    @Column(name = "class_name")
    private String className;

    @Column(name = "academic_year")
    private Integer academicYear;

    @Column(name = "student_status")
    @Enumerated(EnumType.STRING)
    private StudentStatus studentStatus;

    public enum StudentStatus {
        ACTIVE,
        INACTIVE,
        GRADUATED
    }
    @PrePersist
    protected void onCreate() {
        if (studentStatus == null) {
            studentStatus = StudentStatus.ACTIVE;
        }
    }

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;
}
