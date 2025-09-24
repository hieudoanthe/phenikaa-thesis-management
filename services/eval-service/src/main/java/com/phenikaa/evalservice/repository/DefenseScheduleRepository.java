package com.phenikaa.evalservice.repository;

import com.phenikaa.evalservice.entity.DefenseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DefenseScheduleRepository extends JpaRepository<DefenseSchedule, Integer> {

    // Tìm lịch bảo vệ theo năm học
    List<DefenseSchedule> findByAcademicYearId(Integer academicYearId);

    // Tìm lịch bảo vệ theo trạng thái
    List<DefenseSchedule> findByStatus(DefenseSchedule.ScheduleStatus status);

    // Tìm lịch bảo vệ đang hoạt động
    @Query("SELECT ds FROM DefenseSchedule ds WHERE ds.status = 'ACTIVE' AND :now BETWEEN ds.startDate AND ds.endDate")
    Optional<DefenseSchedule> findActiveSchedule(@Param("now") LocalDate now);

    // Tìm lịch bảo vệ theo khoảng thời gian
    @Query("SELECT ds FROM DefenseSchedule ds WHERE ds.startDate >= :startDate AND ds.endDate <= :endDate")
    List<DefenseSchedule> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
