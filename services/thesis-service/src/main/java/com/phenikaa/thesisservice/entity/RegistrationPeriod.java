package com.phenikaa.thesisservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registration_period", schema = "HieuDT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class RegistrationPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "period_id")
    private Integer periodId;

    @Column(name = "period_name", nullable = false)
    private String periodName; // Ví dụ: "Đợt 1 - Học kỳ 1", "Đợt 2 - Học kỳ 1"

    @Column(name = "academic_year_id")
    private Integer academicYearId;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "max_students_per_lecturer", nullable = false)
    private Integer maxStudentsPerLecturer = 15; // Mặc định 15 sinh viên/giảng viên/đợt

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PeriodStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PeriodStatus {
        UPCOMING,    // Sắp diễn ra
        ACTIVE,      // Đang diễn ra
        CLOSED,      // Đã kết thúc
        CANCELLED    // Đã hủy
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = PeriodStatus.UPCOMING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Kiểm tra xem đợt đăng ký có đang hoạt động không
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == PeriodStatus.ACTIVE && 
               now.isAfter(startDate) && 
               now.isBefore(endDate);
    }

    // Kiểm tra xem đợt đăng ký có thể bắt đầu không
    public boolean canStart() {
        LocalDateTime now = LocalDateTime.now();
        return status == PeriodStatus.UPCOMING && now.isAfter(startDate);
    }

    // Kiểm tra xem đợt đăng ký có thể kết thúc không
    public boolean canClose() {
        // Admin có thể kết thúc bất cứ lúc nào nếu period đang ACTIVE
        return status == PeriodStatus.ACTIVE;
    }

    // Kiểm tra xem đợt đăng ký có nên tự động kết thúc không
    public boolean shouldAutoClose() {
        LocalDateTime now = LocalDateTime.now();
        return status == PeriodStatus.ACTIVE && now.isAfter(endDate);
    }
}
