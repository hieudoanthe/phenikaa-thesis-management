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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "student_id")
    private String studentId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "major", columnDefinition = "NVARCHAR(255)")
    private String major;

    @Column(name = "class_name", columnDefinition = "NVARCHAR(255)")
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
        if (major == null) {
            major = "Công nghệ thông tin";
        }
    }

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phoneNumber;

    @Column(name = "avt")
    private String avt;
}
