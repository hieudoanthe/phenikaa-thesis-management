package com.phenikaa.submissionservice.repository;

import com.phenikaa.submissionservice.entity.ReportSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportSubmissionRepository extends JpaRepository<ReportSubmission, Integer> {

    // Tìm báo cáo theo topic ID
    List<ReportSubmission> findByTopicId(Integer topicId);

    // Tìm báo cáo theo người nộp
    List<ReportSubmission> findBySubmittedBy(Integer submittedBy);

    // Tìm báo cáo theo topic, người nộp và loại
    Optional<ReportSubmission> findByTopicIdAndSubmittedByAndSubmissionType(Integer topicId, Integer submittedBy, Integer submissionType);

    // Đếm báo cáo theo khoảng thời gian
    long countBySubmittedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Đếm báo cáo theo người nộp
    long countBySubmittedBy(Integer submittedBy);

    // Đếm báo cáo theo topic
    long countByTopicId(Integer topicId);

    // Đếm báo cáo theo trạng thái
    long countByStatus(Integer status);

    // Tìm báo cáo theo trạng thái
    List<ReportSubmission> findByStatus(Integer status);

    // Tìm báo cáo theo loại
    List<ReportSubmission> findBySubmissionType(Integer submissionType);

    // Tìm báo cáo đã nộp muộn
    @Query("SELECT s FROM ReportSubmission s WHERE s.deadline < s.submittedAt")
    List<ReportSubmission> findLateSubmissions();

    // Tìm báo cáo sắp đến hạn
    @Query("SELECT s FROM ReportSubmission s WHERE s.deadline BETWEEN :startDate AND :endDate")
    List<ReportSubmission> findUpcomingDeadlines(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Tìm báo cáo theo khoảng thời gian
    List<ReportSubmission> findBySubmittedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Tìm báo cáo mới nhất của một topic
    Optional<ReportSubmission> findFirstByTopicIdOrderBySubmittedAtDesc(Integer topicId);

    // Tìm báo cáo theo người nộp và trạng thái
    List<ReportSubmission> findBySubmittedByAndStatus(Integer submittedBy, Integer status);

    // Tìm báo cáo theo topic và trạng thái
    List<ReportSubmission> findByTopicIdAndStatus(Integer topicId, Integer status);
}