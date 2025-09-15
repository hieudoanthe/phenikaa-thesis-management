package com.phenikaa.evalservice.repository;

import com.phenikaa.evalservice.entity.StudentDefense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentDefenseRepository extends JpaRepository<StudentDefense, Integer> {

    // Tìm sinh viên theo buổi bảo vệ
    List<StudentDefense> findByDefenseSession_SessionId(Integer sessionId);

    // Tìm sinh viên theo ID
    List<StudentDefense> findByStudentId(Integer studentId);

    // Tìm sinh viên theo đề tài
    Optional<StudentDefense> findByTopicId(Integer topicId);

    // Tìm sinh viên theo giảng viên hướng dẫn
    List<StudentDefense> findBySupervisorId(Integer supervisorId);

    // Tìm sinh viên theo chuyên ngành
    List<StudentDefense> findByStudentMajor(String major);

    // Tìm sinh viên theo trạng thái
    List<StudentDefense> findByStatus(StudentDefense.DefenseStatus status);

    // Tìm sinh viên theo thời gian bảo vệ
    List<StudentDefense> findByDefenseTime(LocalDateTime defenseTime);

    // Đếm số sinh viên trong buổi bảo vệ
    long countByDefenseSession_SessionId(Integer sessionId);

    // Tìm sinh viên theo thứ tự bảo vệ
    Optional<StudentDefense> findByDefenseSession_SessionIdAndDefenseOrder(Integer sessionId, Integer defenseOrder);

    // Tìm sinh viên theo lịch bảo vệ
    @Query("SELECT sd FROM StudentDefense sd WHERE sd.defenseSession.defenseSchedule.scheduleId = :scheduleId")
    List<StudentDefense> findByScheduleId(@Param("scheduleId") Integer scheduleId);

    // Tìm sinh viên chưa có điểm
    @Query("SELECT sd FROM StudentDefense sd WHERE sd.score IS NULL AND sd.status = 'COMPLETED'")
    List<StudentDefense> findStudentsWithoutScore();

    // Kiểm tra sinh viên đã được gán vào buổi bảo vệ chưa
    boolean existsByDefenseSession_SessionIdAndStudentId(Integer sessionId, Integer studentId);

    // Tìm assignment của sinh viên trong buổi bảo vệ
    Optional<StudentDefense> findByDefenseSession_SessionIdAndStudentId(Integer sessionId, Integer studentId);

    // Tìm danh sách sinh viên đã gán theo thứ tự bảo vệ
    List<StudentDefense> findByDefenseSession_SessionIdOrderByDefenseOrder(Integer sessionId);
    
    // Lấy tất cả StudentDefense với DefenseSession được fetch
    @Query("SELECT sd FROM StudentDefense sd LEFT JOIN FETCH sd.defenseSession")
    List<StudentDefense> findAllWithDefenseSession();
}
