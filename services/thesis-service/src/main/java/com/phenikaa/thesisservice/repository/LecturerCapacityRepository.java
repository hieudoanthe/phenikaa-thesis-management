package com.phenikaa.thesisservice.repository;

import com.phenikaa.thesisservice.entity.LecturerCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LecturerCapacityRepository extends JpaRepository<LecturerCapacity, Integer> {
    
    // Tìm capacity của giảng viên trong một đợt đăng ký cụ thể
    Optional<LecturerCapacity> findByLecturerIdAndRegistrationPeriodId(
        Integer lecturerId, Integer registrationPeriodId);
    
    // Tìm tất cả capacity của một giảng viên
    List<LecturerCapacity> findAllByLecturerId(Integer lecturerId);
    
    // Tìm capacity của giảng viên (lấy capacity mới nhất)
    @Query("SELECT lc FROM LecturerCapacity lc WHERE lc.lecturerId = :lecturerId ORDER BY lc.createdAt DESC")
    Optional<LecturerCapacity> findByLecturerId(@Param("lecturerId") Integer lecturerId);
    
    // Tìm tất cả capacity trong một đợt đăng ký
    List<LecturerCapacity> findByRegistrationPeriodId(Integer registrationPeriodId);
    
    // Kiểm tra xem giảng viên có thể nhận thêm sinh viên trong đợt đăng ký không
    @Query("SELECT lc FROM LecturerCapacity lc WHERE lc.lecturerId = :lecturerId AND lc.registrationPeriodId = :periodId AND lc.currentStudents < lc.maxStudents")
    Optional<LecturerCapacity> findAvailableCapacity(@Param("lecturerId") Integer lecturerId, @Param("periodId") Integer periodId);
}
