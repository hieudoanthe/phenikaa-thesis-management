package com.phenikaa.thesisservice.repository;

import com.phenikaa.thesisservice.entity.RegistrationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Integer> {
    
    // Tìm đợt đăng ký đang hoạt động hoặc sắp diễn ra
    @Query("SELECT rp FROM RegistrationPeriod rp WHERE (rp.status = 'ACTIVE' OR rp.status = 'UPCOMING') AND rp.startDate <= :now AND rp.endDate >= :now ORDER BY rp.startDate ASC")
    Optional<RegistrationPeriod> findActivePeriod(@Param("now") LocalDateTime now);
    
    // Tìm đợt đăng ký theo năm học
    List<RegistrationPeriod> findByAcademicYearId(Integer academicYearId);
    
    // Tìm đợt đăng ký theo trạng thái
    List<RegistrationPeriod> findByStatus(RegistrationPeriod.PeriodStatus status);
    
    // Tìm đợt đăng ký sắp diễn ra
    @Query("SELECT rp FROM RegistrationPeriod rp WHERE rp.status = 'UPCOMING' AND rp.startDate > :now ORDER BY rp.startDate ASC")
    List<RegistrationPeriod> findUpcomingPeriods(@Param("now") LocalDateTime now);
}
