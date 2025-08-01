package com.phenikaa.profileservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teacher_profile", schema = "HieuDT")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TeacherProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id")
    private Integer teacherId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "teacher_code", unique = true)
    private String teacherCode;

    @Column(name = "department")
    private String department;

    @Column(name = "academic_title")
    private String academicTitle;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "teacher_status")
    @Enumerated(EnumType.STRING)
    private TeacherStatus teacherStatus;

    public enum TeacherStatus {
        ACTIVE,
        INACTIVE,
        RETIRED
    }

    @PrePersist
    protected void onCreate() {
        if (teacherStatus == null) {
            teacherStatus = TeacherStatus.ACTIVE;
        }
        if (maxStudents == null || maxStudents > 15) {
            maxStudents = 15;
        }
    }

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;
}
