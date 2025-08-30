package com.phenikaa.thesisservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lecturer_capacity", schema = "HieuDT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class LecturerCapacity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "capacity_id")
    private Integer capacityId;

    @Column(name = "lecturer_id", nullable = false)
    private Integer lecturerId;

    @Column(name = "registration_period_id", nullable = false)
    private Integer registrationPeriodId;

    @Column(name = "max_students", nullable = false)
    private Integer maxStudents = 15; // Mặc định 15 sinh viên/đợt

    @Column(name = "current_students", nullable = false)
    private Integer currentStudents = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Kiểm tra xem giảng viên còn có thể nhận thêm sinh viên không
    public boolean canAcceptMoreStudents() {
        return currentStudents < maxStudents;
    }

    // Lấy số lượng sinh viên còn lại có thể nhận
    public Integer getRemainingSlots() {
        return Math.max(0, maxStudents - currentStudents);
    }

    // Tăng số lượng sinh viên hiện tại
    public boolean increaseCurrentStudents() {
        if (canAcceptMoreStudents()) {
            currentStudents++;
            return true;
        }
        return false;
    }

    // Giảm số lượng sinh viên hiện tại
    public boolean decreaseCurrentStudents() {
        if (currentStudents > 0) {
            currentStudents--;
            return true;
        }
        return false;
    }
}
