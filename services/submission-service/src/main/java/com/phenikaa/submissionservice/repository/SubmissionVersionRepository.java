package com.phenikaa.submissionservice.repository;

import com.phenikaa.submissionservice.entity.SubmissionVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionVersionRepository extends JpaRepository<SubmissionVersion, Integer> {
    
    // Tìm tất cả phiên bản của một submission
    List<SubmissionVersion> findBySubmissionIdOrderByVersionNumberDesc(Integer submissionId);
    
    // Tìm phiên bản hiện tại
    Optional<SubmissionVersion> findBySubmissionIdAndIsCurrentTrue(Integer submissionId);
    
    // Tìm phiên bản theo số phiên bản
    Optional<SubmissionVersion> findBySubmissionIdAndVersionNumber(Integer submissionId, Integer versionNumber);
    
    // Tìm phiên bản mới nhất
    Optional<SubmissionVersion> findFirstBySubmissionIdOrderByVersionNumberDesc(Integer submissionId);
    
    // Đếm số phiên bản
    long countBySubmissionId(Integer submissionId);
    
    // Tìm phiên bản theo người tạo
    List<SubmissionVersion> findByCreatedBy(Integer createdBy);
    
    // Tìm phiên bản trong khoảng thời gian
    @Query("SELECT sv FROM SubmissionVersion sv WHERE sv.createdAt BETWEEN :startDate AND :endDate")
    List<SubmissionVersion> findVersionsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                                   @Param("endDate") java.time.LocalDateTime endDate);
}
