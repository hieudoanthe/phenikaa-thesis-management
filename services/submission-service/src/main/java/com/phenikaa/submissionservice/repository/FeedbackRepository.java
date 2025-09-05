package com.phenikaa.submissionservice.repository;

import com.phenikaa.submissionservice.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    // Tìm phản hồi theo submission ID
    List<Feedback> findBySubmissionSubmissionId(Integer submissionId);

    // Tìm phản hồi theo reviewer ID
    List<Feedback> findByReviewerId(Integer reviewerId);

    // Tìm phản hồi theo loại feedback
    List<Feedback> findByFeedbackType(Integer feedbackType);

    // Tìm phản hồi đã được duyệt
    List<Feedback> findByIsApprovedTrue();

    // Tìm phản hồi chưa được duyệt
    List<Feedback> findByIsApprovedFalse();

    // Tìm phản hồi theo khoảng thời gian
    List<Feedback> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Tìm phản hồi mới nhất của một submission
    Optional<Feedback> findFirstBySubmissionSubmissionIdOrderByCreatedAtDesc(Integer submissionId);

    // Đếm số phản hồi theo reviewer
    long countByReviewerId(Integer reviewerId);

    // Đếm số phản hồi đã duyệt
    long countByIsApprovedTrue();

    // Đếm số phản hồi theo loại
    long countByFeedbackType(Integer feedbackType);

    // Đếm số phản hồi theo khoảng thời gian
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Tìm phản hồi có điểm số
    @Query("SELECT f FROM Feedback f WHERE f.score IS NOT NULL")
    List<Feedback> findFeedbacksWithScore();

    // Tính điểm trung bình của một submission
    @Query("SELECT AVG(f.score) FROM Feedback f WHERE f.submission.submissionId = :submissionId AND f.score IS NOT NULL")
    Optional<Double> findAverageScoreBySubmissionId(@Param("submissionId") Integer submissionId);

    // Tìm phản hồi theo submission và reviewer
    Optional<Feedback> findBySubmissionSubmissionIdAndReviewerId(Integer submissionId, Integer reviewerId);

    // Tìm phản hồi theo reviewer và trạng thái duyệt
    List<Feedback> findByReviewerIdAndIsApproved(Integer reviewerId, Boolean isApproved);

    // Tìm phản hồi theo submission và loại
    List<Feedback> findBySubmissionSubmissionIdAndFeedbackType(Integer submissionId, Integer feedbackType);

    // Tìm phản hồi có điểm số cao nhất
    @Query("SELECT f FROM Feedback f WHERE f.score = (SELECT MAX(f2.score) FROM Feedback f2)")
    List<Feedback> findHighestScoreFeedbacks();

    // Tìm phản hồi có điểm số thấp nhất
    @Query("SELECT f FROM Feedback f WHERE f.score = (SELECT MIN(f2.score) FROM Feedback f2)")
    List<Feedback> findLowestScoreFeedbacks();

    // Tìm phản hồi theo khoảng điểm
    @Query("SELECT f FROM Feedback f WHERE f.score BETWEEN :minScore AND :maxScore")
    List<Feedback> findByScoreBetween(@Param("minScore") Float minScore, @Param("maxScore") Float maxScore);

    // Tìm phản hồi theo submission và trạng thái duyệt
    List<Feedback> findBySubmissionSubmissionIdAndIsApproved(Integer submissionId, Boolean isApproved);

    // Tìm phản hồi theo reviewer và loại
    List<Feedback> findByReviewerIdAndFeedbackType(Integer reviewerId, Integer feedbackType);

    // Tìm phản hồi theo reviewer và khoảng thời gian
    List<Feedback> findByReviewerIdAndCreatedAtBetween(Integer reviewerId, LocalDateTime startDate, LocalDateTime endDate);

    // Tìm phản hồi theo submission và khoảng thời gian
    List<Feedback> findBySubmissionSubmissionIdAndCreatedAtBetween(Integer submissionId, LocalDateTime startDate, LocalDateTime endDate);

    // Tìm phản hồi theo loại và trạng thái duyệt
    List<Feedback> findByFeedbackTypeAndIsApproved(Integer feedbackType, Boolean isApproved);

    // Tìm phản hồi theo loại và khoảng thời gian
    List<Feedback> findByFeedbackTypeAndCreatedAtBetween(Integer feedbackType, LocalDateTime startDate, LocalDateTime endDate);

    // Tìm phản hồi theo trạng thái duyệt và khoảng thời gian
    List<Feedback> findByIsApprovedAndCreatedAtBetween(Boolean isApproved, LocalDateTime startDate, LocalDateTime endDate);

    // Tìm phản hồi theo reviewer, loại và trạng thái duyệt
    List<Feedback> findByReviewerIdAndFeedbackTypeAndIsApproved(Integer reviewerId, Integer feedbackType, Boolean isApproved);

    // Tìm phản hồi theo submission, loại và trạng thái duyệt
    List<Feedback> findBySubmissionSubmissionIdAndFeedbackTypeAndIsApproved(Integer submissionId, Integer feedbackType, Boolean isApproved);

    // Tìm phản hồi theo reviewer, loại và khoảng thời gian
    List<Feedback> findByReviewerIdAndFeedbackTypeAndCreatedAtBetween(Integer reviewerId, Integer feedbackType, LocalDateTime startDate, LocalDateTime endDate);

    // Tìm phản hồi theo submission, loại và khoảng thời gian
    List<Feedback> findBySubmissionSubmissionIdAndFeedbackTypeAndCreatedAtBetween(Integer submissionId, Integer feedbackType, LocalDateTime startDate, LocalDateTime endDate);

    // Tìm phản hồi theo reviewer, trạng thái duyệt và khoảng thời gian
    List<Feedback> findByReviewerIdAndIsApprovedAndCreatedAtBetween(Integer reviewerId, Boolean isApproved, LocalDateTime startDate, LocalDateTime endDate);

    // Tìm phản hồi theo submission, trạng thái duyệt và khoảng thời gian
    List<Feedback> findBySubmissionSubmissionIdAndIsApprovedAndCreatedAtBetween(Integer submissionId, Boolean isApproved, LocalDateTime startDate, LocalDateTime endDate);

    // Tìm phản hồi theo loại, trạng thái duyệt và khoảng thời gian
    List<Feedback> findByFeedbackTypeAndIsApprovedAndCreatedAtBetween(Integer feedbackType, Boolean isApproved, LocalDateTime startDate, LocalDateTime endDate);

    // Tìm phản hồi theo reviewer, loại, trạng thái duyệt và khoảng thời gian
    List<Feedback> findByReviewerIdAndFeedbackTypeAndIsApprovedAndCreatedAtBetween(Integer reviewerId, Integer feedbackType, Boolean isApproved, LocalDateTime startDate, LocalDateTime endDate);

    // Tìm phản hồi theo submission, loại, trạng thái duyệt và khoảng thời gian
    List<Feedback> findBySubmissionSubmissionIdAndFeedbackTypeAndIsApprovedAndCreatedAtBetween(Integer submissionId, Integer feedbackType, Boolean isApproved, LocalDateTime startDate, LocalDateTime endDate);
}