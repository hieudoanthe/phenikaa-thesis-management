package com.phenikaa.thesisservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_preference", schema = "HieuDT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "areas", columnDefinition = "nvarchar(1000)")
    private String areas; // CSV or JSON

    @Column(name = "keywords", columnDefinition = "nvarchar(1000)")
    private String keywords; // CSV or JSON

    @Column(name = "types", columnDefinition = "nvarchar(500)")
    private String types; // CSV or JSON
}


