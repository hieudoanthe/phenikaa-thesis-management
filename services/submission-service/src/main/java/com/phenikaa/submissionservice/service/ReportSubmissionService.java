package com.phenikaa.submissionservice.service;

import com.phenikaa.submissionservice.client.NotificationServiceClient;
import com.phenikaa.submissionservice.dto.request.ReportSubmissionRequest;
import com.phenikaa.submissionservice.dto.response.ReportSubmissionResponse;
import com.phenikaa.submissionservice.entity.ReportSubmission;
import com.phenikaa.submissionservice.repository.ReportSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReportSubmissionService {
    
    private final ReportSubmissionRepository reportSubmissionRepository;
    private final NotificationServiceClient notificationServiceClient;
    
    // Upload directory
    private static final String UPLOAD_DIR = "uploads/reports";
    
    /**
     * Tạo báo cáo mới
     */
    public ReportSubmissionResponse createSubmission(ReportSubmissionRequest request, MultipartFile file) {
        try {
            log.info("Creating new report submission for topic: {}, submitted by: {}", 
                    request.getTopicId(), request.getSubmittedBy());
            
            ReportSubmission submission = new ReportSubmission();
            submission.setTopicId(request.getTopicId());
            submission.setSubmittedBy(request.getSubmittedBy());
            submission.setAssignmentId(request.getAssignmentId());
            submission.setReportTitle(request.getReportTitle());
            submission.setDescription(request.getDescription());
            submission.setSubmissionType(request.getSubmissionType());
            submission.setDeadline(request.getDeadline());
            submission.setStatus(1); // Đã nộp
            submission.setIsFinal(request.getIsFinal());
            submission.setSubmittedAt(LocalDateTime.now());
            
            // Xử lý file upload
            if (file != null && !file.isEmpty()) {
                String filePath = handleFileUpload(file, request.getTopicId());
                submission.setFilePath(filePath);
            }
            
            ReportSubmission savedSubmission = reportSubmissionRepository.save(submission);
            log.info("Report submission created successfully with ID: {}", savedSubmission.getSubmissionId());
            
            // Gửi thông báo qua communication-log-service
            sendSubmissionNotification(savedSubmission, "SUBMISSION_CREATED");
            
            return convertToResponse(savedSubmission);
            
        } catch (Exception e) {
            log.error("Error creating report submission: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo báo cáo: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật báo cáo
     */
    public ReportSubmissionResponse updateSubmission(Integer submissionId, ReportSubmissionRequest request, MultipartFile file) {
        try {
            log.info("Updating report submission: {}", submissionId);
            
            ReportSubmission submission = reportSubmissionRepository.findById(submissionId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo với ID: " + submissionId));
            
            // Cập nhật thông tin
            submission.setReportTitle(request.getReportTitle());
            submission.setDescription(request.getDescription());
            submission.setSubmissionType(request.getSubmissionType());
            submission.setDeadline(request.getDeadline());
            submission.setIsFinal(request.getIsFinal());
            
            // Xử lý file mới nếu có
            if (file != null && !file.isEmpty()) {
                // Xóa file cũ nếu có
                if (submission.getFilePath() != null) {
                    deleteFile(submission.getFilePath());
                }
                String filePath = handleFileUpload(file, submission.getTopicId());
                submission.setFilePath(filePath);
            }
            
            ReportSubmission savedSubmission = reportSubmissionRepository.save(submission);
            log.info("Report submission updated successfully: {}", savedSubmission.getSubmissionId());
            
            return convertToResponse(savedSubmission);
            
        } catch (Exception e) {
            log.error("Error updating report submission: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật báo cáo: " + e.getMessage());
        }
    }
    
    /**
     * Lấy báo cáo theo ID
     */
    public ReportSubmissionResponse getSubmissionById(Integer submissionId) {
        ReportSubmission submission = reportSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo với ID: " + submissionId));
        
        return convertToResponse(submission);
    }
    
    /**
     * Lấy tất cả báo cáo theo topic
     */
    public List<ReportSubmissionResponse> getSubmissionsByTopic(Integer topicId) {
        List<ReportSubmission> submissions = reportSubmissionRepository.findByTopicId(topicId);
        return submissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy báo cáo theo người nộp
     */
    public List<ReportSubmissionResponse> getSubmissionsByUser(Integer userId) {
        List<ReportSubmission> submissions = reportSubmissionRepository.findBySubmittedBy(userId);
        return submissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy báo cáo với phân trang
     */
    public Page<ReportSubmissionResponse> getSubmissionsWithPagination(Pageable pageable) {
        Page<ReportSubmission> submissions = reportSubmissionRepository.findAll(pageable);
        return submissions.map(this::convertToResponse);
    }
    
    /**
     * Xóa báo cáo
     */
    public void deleteSubmission(Integer submissionId) {
        try {
            log.info("Deleting report submission: {}", submissionId);
            
            ReportSubmission submission = reportSubmissionRepository.findById(submissionId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo với ID: " + submissionId));
            
            // Xóa file nếu có
            if (submission.getFilePath() != null) {
                deleteFile(submission.getFilePath());
            }
            
            reportSubmissionRepository.delete(submission);
            log.info("Report submission deleted successfully: {}", submissionId);
            
        } catch (Exception e) {
            log.error("Error deleting report submission: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể xóa báo cáo: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật trạng thái báo cáo
     */
    public ReportSubmissionResponse updateSubmissionStatus(Integer submissionId, Integer status) {
        try {
            log.info("Updating submission status: {} to {}", submissionId, status);
            
            ReportSubmission submission = reportSubmissionRepository.findById(submissionId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo với ID: " + submissionId));
            
            submission.setStatus(status);
            ReportSubmission savedSubmission = reportSubmissionRepository.save(submission);
            
            log.info("Submission status updated successfully: {}", submissionId);
            return convertToResponse(savedSubmission);
            
        } catch (Exception e) {
            log.error("Error updating submission status: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật trạng thái báo cáo: " + e.getMessage());
        }
    }
    
    /**
     * Xử lý upload file
     */
    private String handleFileUpload(MultipartFile file, Integer topicId) throws IOException {
        // Tạo thư mục nếu chưa có
        Path uploadPath = Paths.get(UPLOAD_DIR, "topic_" + topicId);
        Files.createDirectories(uploadPath);
        
        // Tạo tên file unique
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Lưu file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);
        
        return filePath.toString();
    }
    
    /**
     * Xóa file
     */
    private void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("File deleted: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage());
        }
    }
    
    /**
     * Convert entity to response DTO
     */
    private ReportSubmissionResponse convertToResponse(ReportSubmission submission) {
        ReportSubmissionResponse response = ReportSubmissionResponse.builder()
                .submissionId(submission.getSubmissionId())
                .topicId(submission.getTopicId())
                .submittedBy(submission.getSubmittedBy())
                .assignmentId(submission.getAssignmentId())
                .reportTitle(submission.getReportTitle())
                .description(submission.getDescription())
                .filePath(submission.getFilePath())
                .submissionType(submission.getSubmissionType())
                .submittedAt(submission.getSubmittedAt())
                .deadline(submission.getDeadline())
                .status(submission.getStatus())
                .isFinal(submission.getIsFinal())
                .build();
        
        // Thông tin bổ sung sẽ được tự động tính toán bởi getter methods
        
        // Thêm phản hồi nếu có
        if (submission.getFeedbacks() != null && !submission.getFeedbacks().isEmpty()) {
            response.setFeedbackCount(submission.getFeedbacks().size());
            response.setHasFeedback(true);
        } else {
            response.setFeedbackCount(0);
            response.setHasFeedback(false);
        }
        
        return response;
    }
    
    /**
     * Gửi thông báo qua communication-log-service
     */
    private void sendSubmissionNotification(ReportSubmission submission, String notificationType) {
        try {
            Map<String, Object> notification = Map.of(
                "type", notificationType,
                "receiverId", submission.getSubmittedBy(),
                "message", String.format("Báo cáo '%s' đã được %s", 
                    submission.getReportTitle(), 
                    notificationType.equals("SUBMISSION_CREATED") ? "tạo mới" : "cập nhật"),
                "data", Map.of(
                    "submissionId", submission.getSubmissionId(),
                    "topicId", submission.getTopicId(),
                    "reportTitle", submission.getReportTitle(),
                    "submittedAt", submission.getSubmittedAt()
                )
            );
            
            notificationServiceClient.sendNotification(notification);
            log.info("Notification sent for submission: {}", submission.getSubmissionId());
            
        } catch (Exception e) {
            log.error("Error sending notification for submission {}: {}", 
                submission.getSubmissionId(), e.getMessage());
        }
    }
}
