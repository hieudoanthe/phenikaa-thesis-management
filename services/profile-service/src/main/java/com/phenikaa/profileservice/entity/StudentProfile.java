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

    @Column(name = "major", columnDefinition = "nvarchar(255)")
    private String major;

    @Column(name = "class_name", columnDefinition = "nvarchar(255)")
    private String className;

    @Column(name = "phone")
    private String phoneNumber;

    @Column(name = "avt")
    private String avt;
}
