package com.phenikaa.submissionservice.service;

import com.phenikaa.submissionservice.client.NotificationServiceClient;
import com.phenikaa.submissionservice.dto.request.FeedbackRequest;
import com.phenikaa.submissionservice.dto.response.FeedbackResponse;
import com.phenikaa.submissionservice.entity.Feedback;
import com.phenikaa.submissionservice.entity.ReportSubmission;
import com.phenikaa.submissionservice.repository.FeedbackRepository;
import com.phenikaa.submissionservice.repository.ReportSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    private final ReportSubmissionRepository reportSubmissionRepository;
    private final NotificationServiceClient communicationServiceClient;
    
    /**
     * Tạo phản hồi mới
     */
    public FeedbackResponse createFeedback(FeedbackRequest request) {
        try {
            log.info("Creating new feedback for submission: {}, reviewer: {}", 
                    request.getSubmissionId(), request.getReviewerId());
            
            // Kiểm tra submission có tồn tại không
            ReportSubmission submission = reportSubmissionRepository.findById(request.getSubmissionId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo với ID: " + request.getSubmissionId()));
            
            Feedback feedback = new Feedback();
            feedback.setSubmission(submission);
            feedback.setReviewerId(request.getReviewerId());
            feedback.setContent(request.getContent());
            feedback.setScore(request.getScore());
            feedback.setFeedbackType(request.getFeedbackType());
            feedback.setIsApproved(request.getIsApproved());
            feedback.setCreatedAt(LocalDateTime.now());
            feedback.setUpdatedAt(LocalDateTime.now());
            
            Feedback savedFeedback = feedbackRepository.save(feedback);
            log.info("Feedback created successfully with ID: {}", savedFeedback.getFeedbackId());
            
            // Gửi thông báo qua communication-log-service
            sendFeedbackNotification(savedFeedback, "FEEDBACK_CREATED");
            
            return convertToResponse(savedFeedback);
            
        } catch (Exception e) {
            log.error("Error creating feedback: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo phản hồi: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật phản hồi
     */
    public FeedbackResponse updateFeedback(Integer feedbackId, FeedbackRequest request) {
        try {
            log.info("Updating feedback: {}", feedbackId);
            
            Feedback feedback = feedbackRepository.findById(feedbackId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phản hồi với ID: " + feedbackId));
            
            // Cập nhật thông tin
            feedback.setContent(request.getContent());
            feedback.setScore(request.getScore());
            feedback.setFeedbackType(request.getFeedbackType());
            feedback.setIsApproved(request.getIsApproved());
            feedback.setUpdatedAt(LocalDateTime.now());
            
            Feedback savedFeedback = feedbackRepository.save(feedback);
            log.info("Feedback updated successfully: {}", savedFeedback.getFeedbackId());
            
            return convertToResponse(savedFeedback);
            
        } catch (Exception e) {
            log.error("Error updating feedback: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật phản hồi: " + e.getMessage());
        }
    }
    
    /**
     * Lấy phản hồi theo ID
     */
    public FeedbackResponse getFeedbackById(Integer feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phản hồi với ID: " + feedbackId));
        
        return convertToResponse(feedback);
    }
    
    /**
     * Lấy tất cả phản hồi theo submission
     */
    public List<FeedbackResponse> getFeedbacksBySubmission(Integer submissionId) {
        List<Feedback> feedbacks = feedbackRepository.findBySubmissionSubmissionId(submissionId);
        return feedbacks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy phản hồi theo reviewer
     */
    public List<FeedbackResponse> getFeedbacksByReviewer(Integer reviewerId) {
        List<Feedback> feedbacks = feedbackRepository.findByReviewerId(reviewerId);
        return feedbacks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy phản hồi với phân trang
     */
    public Page<FeedbackResponse> getFeedbacksWithPagination(Pageable pageable) {
        Page<Feedback> feedbacks = feedbackRepository.findAll(pageable);
        return feedbacks.map(this::convertToResponse);
    }
    
    /**
     * Xóa phản hồi
     */
    public void deleteFeedback(Integer feedbackId) {
        try {
            log.info("Deleting feedback: {}", feedbackId);
            
            Feedback feedback = feedbackRepository.findById(feedbackId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phản hồi với ID: " + feedbackId));
            
            feedbackRepository.delete(feedback);
            log.info("Feedback deleted successfully: {}", feedbackId);
            
        } catch (Exception e) {
            log.error("Error deleting feedback: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể xóa phản hồi: " + e.getMessage());
        }
    }
    
    /**
     * Duyệt phản hồi
     */
    public FeedbackResponse approveFeedback(Integer feedbackId) {
        try {
            log.info("Approving feedback: {}", feedbackId);
            
            Feedback feedback = feedbackRepository.findById(feedbackId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phản hồi với ID: " + feedbackId));
            
            feedback.setIsApproved(true);
            feedback.setUpdatedAt(LocalDateTime.now());
            
            Feedback savedFeedback = feedbackRepository.save(feedback);
            log.info("Feedback approved successfully: {}", feedbackId);
            
            return convertToResponse(savedFeedback);
            
        } catch (Exception e) {
            log.error("Error approving feedback: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể duyệt phản hồi: " + e.getMessage());
        }
    }
    
    /**
     * Tính điểm trung bình của submission
     */
    public Double calculateAverageScore(Integer submissionId) {
        Optional<Double> averageScore = feedbackRepository.findAverageScoreBySubmissionId(submissionId);
        return averageScore.orElse(0.0);
    }
    
    /**
     * Lấy phản hồi mới nhất của submission
     */
    public Optional<FeedbackResponse> getLatestFeedback(Integer submissionId) {
        Optional<Feedback> latestFeedback = feedbackRepository.findFirstBySubmissionSubmissionIdOrderByCreatedAtDesc(submissionId);
        return latestFeedback.map(this::convertToResponse);
    }
    
    /**
     * Kiểm tra xem reviewer đã phản hồi chưa
     */
    public boolean hasReviewerFeedback(Integer submissionId, Integer reviewerId) {
        Optional<Feedback> existingFeedback = feedbackRepository.findBySubmissionSubmissionIdAndReviewerId(submissionId, reviewerId);
        return existingFeedback.isPresent();
    }
    
    /**
     * Convert entity to response DTO
     */
    public FeedbackResponse convertToResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId())
                .submissionId(feedback.getSubmission().getSubmissionId())
                .reviewerId(feedback.getReviewerId())
                .content(feedback.getContent())
                .score(feedback.getScore())
                .feedbackType(feedback.getFeedbackType())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .isApproved(feedback.getIsApproved())
                .build();
    }
    
    /**
     * Gửi thông báo feedback qua communication-log-service
     */
    private void sendFeedbackNotification(Feedback feedback, String notificationType) {
        try {
            // Lấy thông tin submission để gửi thông báo cho người nộp báo cáo
            ReportSubmission submission = feedback.getSubmission();
            
            Map<String, Object> notification = Map.of(
                "type", notificationType,
                "receiverId", submission.getSubmittedBy(),
                "message", String.format("Có phản hồi mới cho báo cáo '%s'", submission.getReportTitle()),
                "data", Map.of(
                    "feedbackId", feedback.getFeedbackId(),
                    "submissionId", submission.getSubmissionId(),
                    "reviewerId", feedback.getReviewerId(),
                    "feedbackType", feedback.getFeedbackType(),
                    "score", feedback.getScore(),
                    "createdAt", feedback.getCreatedAt()
                )
            );
            
            communicationServiceClient.sendNotification(notification);
            log.info("Feedback notification sent for feedback: {}", feedback.getFeedbackId());
            
        } catch (Exception e) {
            log.error("Error sending feedback notification for feedback {}: {}", 
                feedback.getFeedbackId(), e.getMessage());
        }
    }
}
