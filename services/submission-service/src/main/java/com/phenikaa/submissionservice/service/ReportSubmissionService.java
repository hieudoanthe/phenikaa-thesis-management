package com.phenikaa.submissionservice.service;

import com.phenikaa.submissionservice.client.CommunicationServiceClient;
import com.phenikaa.submissionservice.dto.request.ReportSubmissionRequest;
import com.phenikaa.submissionservice.dto.response.ReportSubmissionResponse;
import com.phenikaa.submissionservice.entity.ReportSubmission;
import com.phenikaa.submissionservice.exception.ReportSubmissionException;
import com.phenikaa.submissionservice.repository.ReportSubmissionRepository;
import com.phenikaa.submissionservice.service.interfaces.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.phenikaa.submissionservice.dto.request.SubmissionFilterRequest;
import com.phenikaa.submissionservice.spec.ReportSubmissionSpecification;
@Service
@Slf4j
@Transactional
public class ReportSubmissionService {
    
    private final ReportSubmissionRepository reportSubmissionRepository;
    private final CommunicationServiceClient communicationServiceClient;
    private final FileStorageService cloudinaryService;
    
    public ReportSubmissionService(
            ReportSubmissionRepository reportSubmissionRepository,
            CommunicationServiceClient communicationServiceClient,
            @Qualifier("cloudinaryFileService") FileStorageService cloudinaryService
    ) {
        this.reportSubmissionRepository = reportSubmissionRepository;
        this.communicationServiceClient = communicationServiceClient;
        this.cloudinaryService = cloudinaryService;
    }
    
    // Constants
    private static final String SUBMISSION_NOT_FOUND_MSG = "Không tìm thấy báo cáo với ID: ";
    
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
                String fileUrl = handleFileUpload(file, request.getTopicId());
                submission.setFilePath(fileUrl);
            }
            
            ReportSubmission savedSubmission = reportSubmissionRepository.save(submission);
            log.info("Report submission created successfully with ID: {}", savedSubmission.getSubmissionId());
            
            // Gửi thông báo qua communication-log-service
            sendSubmissionNotification(savedSubmission, "SUBMISSION_CREATED");
            
            return convertToResponse(savedSubmission);
            
        } catch (Exception e) {
            log.error("Error creating report submission: {}", e.getMessage(), e);
            throw new ReportSubmissionException("Không thể tạo báo cáo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cập nhật báo cáo
     */
    public ReportSubmissionResponse updateSubmission(Integer submissionId, ReportSubmissionRequest request, MultipartFile file) {
        try {
            log.info("Updating report submission: {}", submissionId);
            
            ReportSubmission submission = reportSubmissionRepository.findById(submissionId)
                    .orElseThrow(() -> new ReportSubmissionException(SUBMISSION_NOT_FOUND_MSG + submissionId));
            
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
                String fileUrl = handleFileUpload(file, submission.getTopicId());
                submission.setFilePath(fileUrl);
            }
            
            ReportSubmission savedSubmission = reportSubmissionRepository.save(submission);
            log.info("Report submission updated successfully: {}", savedSubmission.getSubmissionId());
            
            return convertToResponse(savedSubmission);
            
        } catch (Exception e) {
            log.error("Error updating report submission: {}", e.getMessage(), e);
            throw new ReportSubmissionException("Không thể cập nhật báo cáo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lấy báo cáo theo ID
     */
    public ReportSubmissionResponse getSubmissionById(Integer submissionId) {
        ReportSubmission submission = reportSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ReportSubmissionException(SUBMISSION_NOT_FOUND_MSG + submissionId));
        
        return convertToResponse(submission);
    }
    
    /**
     * Lấy tất cả báo cáo theo topic
     */
    public List<ReportSubmissionResponse> getSubmissionsByTopic(Integer topicId) {
        List<ReportSubmission> submissions = reportSubmissionRepository.findByTopicId(topicId);
        return submissions.stream()
                .map(this::convertToResponse)
                .toList();
    }
    
    /**
     * Lấy báo cáo theo người nộp
     */
    public List<ReportSubmissionResponse> getSubmissionsByUser(Integer userId) {
        List<ReportSubmission> submissions = reportSubmissionRepository.findBySubmittedBy(userId);
        return submissions.stream()
                .map(this::convertToResponse)
                .toList();
    }
    
    /**
     * Lấy báo cáo với phân trang
     */
    public Page<ReportSubmissionResponse> getSubmissionsWithPagination(Pageable pageable) {
        Page<ReportSubmission> submissions = reportSubmissionRepository.findAll(pageable);
        return submissions.map(this::convertToResponse);
    }

    /**
     * Tìm kiếm + lọc bằng Specification
     */
    public Page<ReportSubmissionResponse> filterSubmissions(SubmissionFilterRequest req, Integer submittedBy) {
        Specification<ReportSubmission> spec = ReportSubmissionSpecification.withFilter(
                req.getSearch(), req.getSubmissionType(), submittedBy
        );
        PageRequest pageable = PageRequest.of(
                req.getPage() == null ? 0 : req.getPage(),
                req.getSize() == null ? 10 : req.getSize()
        );
        Page<ReportSubmission> page = reportSubmissionRepository.findAll(spec, pageable);
        return page.map(this::convertToResponse);
    }
    
    /**
     * Xóa báo cáo
     */
    public void deleteSubmission(Integer submissionId) {
        try {
            log.info("Deleting report submission: {}", submissionId);
            
            ReportSubmission submission = reportSubmissionRepository.findById(submissionId)
                    .orElseThrow(() -> new ReportSubmissionException(SUBMISSION_NOT_FOUND_MSG + submissionId));
            
            // Xóa file nếu có
            if (submission.getFilePath() != null) {
                deleteFile(submission.getFilePath());
            }
            
            reportSubmissionRepository.delete(submission);
            log.info("Report submission deleted successfully: {}", submissionId);
            
        } catch (Exception e) {
            log.error("Error deleting report submission: {}", e.getMessage(), e);
            throw new ReportSubmissionException("Không thể xóa báo cáo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cập nhật trạng thái báo cáo
     */
    public ReportSubmissionResponse updateSubmissionStatus(Integer submissionId, Integer status) {
        try {
            log.info("Updating submission status: {} to {}", submissionId, status);
            
            ReportSubmission submission = reportSubmissionRepository.findById(submissionId)
                    .orElseThrow(() -> new ReportSubmissionException(SUBMISSION_NOT_FOUND_MSG + submissionId));
            
            submission.setStatus(status);
            ReportSubmission savedSubmission = reportSubmissionRepository.save(submission);
            
            log.info("Submission status updated successfully: {}", submissionId);
            return convertToResponse(savedSubmission);
            
        } catch (Exception e) {
            log.error("Error updating submission status: {}", e.getMessage(), e);
            throw new ReportSubmissionException("Không thể cập nhật trạng thái báo cáo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Xử lý upload file qua FileStorageService (Adapter)
     */
    private String handleFileUpload(MultipartFile file, Integer topicId) {
        try {
            // Validate file size (ví dụ: max 50MB)
            if (file.getSize() > 300 * 1024 * 1024) {
                throw new ReportSubmissionException("File quá lớn. Kích thước tối đa là 50MB");
            }

            // Validate file type (tùy chọn)
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new ReportSubmissionException("Tên file không hợp lệ");
            }

            String folderName = "thesis-reports/topic_" + topicId;
            return cloudinaryService.uploadFile(file, folderName);
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new ReportSubmissionException("Không thể upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa file qua FileStorageService (Adapter)
     */
    private void deleteFile(String fileUrl) {
        try {
            cloudinaryService.deleteFile(fileUrl);
            log.info("File deleted via storage service: {}", fileUrl);
        } catch (Exception e) {
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
            
            communicationServiceClient.sendNotification(notification);
            log.info("Notification sent for submission: {}", submission.getSubmissionId());
            
        } catch (Exception e) {
            log.error("Error sending notification for submission {}: {}", 
                submission.getSubmissionId(), e.getMessage());
        }
    }
}
