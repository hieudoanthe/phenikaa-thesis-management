package com.phenikaa.academicservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "academic_year", schema = "HieuDT" )
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYear {
    @Id
    @Column(name = "year_id")
    private Integer yearId;

    @Column(name = "year_name", length = 255)
    private String yearName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
