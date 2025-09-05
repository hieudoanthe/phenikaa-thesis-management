package com.phenikaa.submissionservice.controller;

import com.phenikaa.submissionservice.dto.request.FeedbackRequest;
import com.phenikaa.submissionservice.dto.response.FeedbackResponse;
import com.phenikaa.submissionservice.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/submission-service")
@RequiredArgsConstructor
@Slf4j
public class FeedbackController {
    
    private final FeedbackService feedbackService;
    
    /**
     * Tạo phản hồi mới
     */
    @PostMapping("/feedbacks")
    public ResponseEntity<FeedbackResponse> createFeedback(@Valid @RequestBody FeedbackRequest request) {
        try {
            log.info("Creating new feedback for submission: {}", request.getSubmissionId());
            FeedbackResponse response = feedbackService.createFeedback(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating feedback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Cập nhật phản hồi
     */
    @PutMapping("/feedbacks/{feedbackId}")
    public ResponseEntity<FeedbackResponse> updateFeedback(
            @PathVariable Integer feedbackId,
            @Valid @RequestBody FeedbackRequest request) {
        try {
            log.info("Updating feedback: {}", feedbackId);
            FeedbackResponse response = feedbackService.updateFeedback(feedbackId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating feedback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Lấy phản hồi theo ID
     */
    @GetMapping("/feedbacks/{feedbackId}")
    public ResponseEntity<FeedbackResponse> getFeedbackById(@PathVariable Integer feedbackId) {
        try {
            FeedbackResponse response = feedbackService.getFeedbackById(feedbackId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting feedback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Lấy phản hồi theo submission
     */
    @GetMapping("/feedbacks/submission/{submissionId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksBySubmission(@PathVariable Integer submissionId) {
        try {
            List<FeedbackResponse> responses = feedbackService.getFeedbacksBySubmission(submissionId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting feedbacks by submission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Lấy phản hồi theo reviewer
     */
    @GetMapping("/feedbacks/reviewer/{reviewerId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksByReviewer(@PathVariable Integer reviewerId) {
        try {
            List<FeedbackResponse> responses = feedbackService.getFeedbacksByReviewer(reviewerId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting feedbacks by reviewer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Lấy phản hồi với phân trang
     */
    @GetMapping("/feedbacks")
    public ResponseEntity<Page<FeedbackResponse>> getFeedbacksWithPagination(Pageable pageable) {
        try {
            Page<FeedbackResponse> responses = feedbackService.getFeedbacksWithPagination(pageable);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting feedbacks with pagination: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Duyệt phản hồi
     */
    @PutMapping("/feedbacks/{feedbackId}/approve")
    public ResponseEntity<FeedbackResponse> approveFeedback(@PathVariable Integer feedbackId) {
        try {
            log.info("Approving feedback: {}", feedbackId);
            FeedbackResponse response = feedbackService.approveFeedback(feedbackId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error approving feedback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Tính điểm trung bình của submission
     */
    @GetMapping("/feedbacks/submission/{submissionId}/average-score")
    public ResponseEntity<Double> getAverageScore(@PathVariable Integer submissionId) {
        try {
            Double averageScore = feedbackService.calculateAverageScore(submissionId);
            return ResponseEntity.ok(averageScore);
        } catch (Exception e) {
            log.error("Error calculating average score: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Xóa phản hồi
     */
    @DeleteMapping("/feedbacks/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Integer feedbackId) {
        try {
            log.info("Deleting feedback: {}", feedbackId);
            feedbackService.deleteFeedback(feedbackId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting feedback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
