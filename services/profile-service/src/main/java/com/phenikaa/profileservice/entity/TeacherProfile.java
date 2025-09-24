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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "teacher_id")
    private String teacherId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "department")
    private String department;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "degree")
    private String degree;

    @Column(name = "phone")
    private String phoneNumber;

    @Column(name = "avt")
    private String avt;
}
