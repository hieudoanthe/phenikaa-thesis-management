package com.phenikaa.evalservice.repository;

import com.phenikaa.evalservice.entity.StudentDefense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentDefenseRepository extends JpaRepository<StudentDefense, Integer> {

    // Tìm sinh viên theo ID sinh viên
    List<StudentDefense> findByStudentId(Integer studentId);

    // Tìm sinh viên theo đề tài
    Optional<StudentDefense> findByTopicId(Integer topicId);

    // Lấy StudentDefense kèm DefenseSession để truy cập địa điểm phòng
    @Query("SELECT sd FROM StudentDefense sd LEFT JOIN FETCH sd.defenseSession WHERE sd.topicId = :topicId")
    Optional<StudentDefense> findWithSessionByTopicId(@Param("topicId") Integer topicId);

    // Tìm sinh viên theo trạng thái
    List<StudentDefense> findByStatus(StudentDefense.DefenseStatus status);

    // Đếm số sinh viên trong buổi bảo vệ
    long countByDefenseSession_SessionId(Integer sessionId);

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
