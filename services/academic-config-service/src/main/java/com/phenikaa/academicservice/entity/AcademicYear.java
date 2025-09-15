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
    private Integer status; // Giữ nguyên Integer để tương thích với database cũ

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum Status {
        ACTIVE(1),     // Đang hoạt động
        INACTIVE(0),   // Không hoạt động
        UPCOMING(2);   // Sắp diễn ra

        private final Integer value;

        Status(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }

        public static Status fromValue(Integer value) {
            for (Status status : Status.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            return INACTIVE; // Default
        }
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = Status.INACTIVE.getValue();
        }
    }

    // Kiểm tra xem năm học có đang active không
    public boolean isActive() {
        return Status.ACTIVE.getValue().equals(status);
    }

    // Kiểm tra xem năm học có thể active không
    public boolean canActivate() {
        LocalDate now = LocalDate.now();
        // Cho phép kích hoạt trước ngày bắt đầu, miễn chưa quá ngày kết thúc
        // Điều kiện: now <= endDate (bao gồm ngày kết thúc)
        return endDate == null || !now.isAfter(endDate);
    }

    // Getter cho status enum
    public Status getStatusEnum() {
        return Status.fromValue(status);
    }

    // Setter cho status enum
    public void setStatusEnum(Status statusEnum) {
        this.status = statusEnum.getValue();
    }
}
