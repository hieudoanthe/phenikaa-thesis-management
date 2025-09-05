package com.phenikaa.submissionservice.controller;

import com.phenikaa.submissionservice.dto.request.ReportSubmissionRequest;
import com.phenikaa.submissionservice.dto.response.ReportSubmissionResponse;
import com.phenikaa.submissionservice.service.ReportSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/submission-service")
@RequiredArgsConstructor
@Slf4j
public class ReportSubmissionController {
    
    private final ReportSubmissionService reportSubmissionService;
    
    /**
     * Tạo báo cáo mới
     */
    @PostMapping(value = "/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportSubmissionResponse> createSubmission(
            @Valid @ModelAttribute ReportSubmissionRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            log.info("Creating new report submission for topic: {}", request.getTopicId());
            ReportSubmissionResponse response = reportSubmissionService.createSubmission(request, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating submission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Cập nhật báo cáo
     */
    @PutMapping(value = "/submissions/{submissionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportSubmissionResponse> updateSubmission(
            @PathVariable Integer submissionId,
            @Valid @ModelAttribute ReportSubmissionRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            log.info("Updating submission: {}", submissionId);
            ReportSubmissionResponse response = reportSubmissionService.updateSubmission(submissionId, request, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating submission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Lấy báo cáo theo ID
     */
    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<ReportSubmissionResponse> getSubmissionById(@PathVariable Integer submissionId) {
        try {
            ReportSubmissionResponse response = reportSubmissionService.getSubmissionById(submissionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting submission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Lấy báo cáo theo topic
     */
    @GetMapping("/submissions/topic/{topicId}")
    public ResponseEntity<List<ReportSubmissionResponse>> getSubmissionsByTopic(@PathVariable Integer topicId) {
        try {
            List<ReportSubmissionResponse> responses = reportSubmissionService.getSubmissionsByTopic(topicId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting submissions by topic: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Lấy báo cáo theo người dùng
     */
    @GetMapping("/submissions/user/{userId}")
    public ResponseEntity<List<ReportSubmissionResponse>> getSubmissionsByUser(@PathVariable Integer userId) {
        try {
            List<ReportSubmissionResponse> responses = reportSubmissionService.getSubmissionsByUser(userId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting submissions by user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Lấy báo cáo với phân trang
     */
    @GetMapping("/submissions")
    public ResponseEntity<Page<ReportSubmissionResponse>> getSubmissionsWithPagination(Pageable pageable) {
        try {
            Page<ReportSubmissionResponse> responses = reportSubmissionService.getSubmissionsWithPagination(pageable);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting submissions with pagination: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Cập nhật trạng thái báo cáo
     */
    @PutMapping("/submissions/{submissionId}/status")
    public ResponseEntity<ReportSubmissionResponse> updateSubmissionStatus(
            @PathVariable Integer submissionId,
            @RequestParam Integer status) {
        try {
            log.info("Updating submission status: {} to {}", submissionId, status);
            ReportSubmissionResponse response = reportSubmissionService.updateSubmissionStatus(submissionId, status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating submission status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Xóa báo cáo
     */
    @DeleteMapping("/submissions/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Integer submissionId) {
        try {
            log.info("Deleting submission: {}", submissionId);
            reportSubmissionService.deleteSubmission(submissionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting submission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
