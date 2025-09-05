package com.phenikaa.evalservice.repository;

import com.phenikaa.evalservice.entity.DefenseSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DefenseSessionRepository extends JpaRepository<DefenseSession, Integer> {

    // Tìm các buổi bảo vệ theo lịch
    List<DefenseSession> findByDefenseSchedule_ScheduleId(Integer scheduleId);

    // Tìm các buổi bảo vệ theo ngày
    List<DefenseSession> findByDefenseDate(LocalDate defenseDate);

    // Tìm các buổi bảo vệ theo khoảng thời gian
    @Query("SELECT ds FROM DefenseSession ds WHERE ds.defenseDate >= :startDate AND ds.defenseDate <= :endDate")
    List<DefenseSession> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Tìm các buổi bảo vệ theo trạng thái
    List<DefenseSession> findByStatus(DefenseSession.SessionStatus status);

    // Tìm các buổi bảo vệ có thể thêm sinh viên (chưa đầy)
    @Query("SELECT ds FROM DefenseSession ds WHERE ds.status = 'SCHEDULED' AND " +
           "(SELECT COUNT(sd) FROM StudentDefense sd WHERE sd.defenseSession.sessionId = ds.sessionId) < ds.maxStudents")
    List<DefenseSession> findAvailableSessions();

    // Tìm buổi bảo vệ theo địa điểm
    List<DefenseSession> findByLocation(String location);
    
    // Thống kê - đếm theo trạng thái
    long countByStatus(DefenseSession.SessionStatus status);
}
