package com.phenikaa.thesisservice.repository;

import com.phenikaa.thesisservice.entity.Register;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegisterRepository extends JpaRepository<Register, Integer> {

    @Query("SELECT r FROM Register r WHERE r.studentId = :studentId AND r.projectTopic.topicId = :topicId")
    List<Register> findByStudentIdAndTopicId(@Param("studentId") Integer studentId, @Param("topicId") Integer topicId);

    @Query("SELECT r FROM Register r WHERE r.studentId = :studentId AND r.registerStatus = 'APPROVED'")
    List<Register> findApprovedRegistrationsByStudentId(@Param("studentId") Integer studentId);

    @Query("SELECT r FROM Register r WHERE r.projectTopic.topicId = :topicId AND r.registerStatus = 'APPROVED'")
    List<Register> findApprovedRegistrationsByTopicId(@Param("topicId") Integer topicId);

    // Kiểm tra xem sinh viên đã đăng ký trong đợt đăng ký chưa
    boolean existsByStudentIdAndRegistrationPeriodId(Integer studentId, Integer registrationPeriodId);
    
    // Lấy danh sách đăng ký theo đợt đăng ký
    List<Register> findByRegistrationPeriodId(Integer registrationPeriodId);
    
    // Statistics methods
    Long countByRegisterStatus(Register.RegisterStatus registerStatus);
    Long countByRegistrationPeriodId(Integer registrationPeriodId);
    List<Register> findByProjectTopicTopicId(Integer topicId);
    
    // Today's statistics
    Long countByRegisteredAtBetween(java.time.Instant start, java.time.Instant end);
    List<Register> findByRegisteredAtBetween(java.time.Instant start, java.time.Instant end);

    // Tìm đăng ký mới nhất của sinh viên
    Optional<Register> findTopByStudentIdOrderByRegisteredAtDesc(Integer studentId);

    // Lấy đăng ký của sinh viên trong một đợt cụ thể
    Optional<Register> findTopByStudentIdAndRegistrationPeriodIdOrderByRegisteredAtDesc(Integer studentId, Integer registrationPeriodId);
}
