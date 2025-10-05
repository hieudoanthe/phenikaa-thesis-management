package com.phenikaa.submissionservice.service;

import com.phenikaa.submissionservice.client.NotificationServiceClient;
import com.phenikaa.submissionservice.client.UserServiceClient;
import com.phenikaa.submissionservice.client.ThesisServiceClient;
import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.submissionservice.dto.request.ReportSubmissionRequest;
import com.phenikaa.submissionservice.dto.response.ReportSubmissionResponse;
import com.phenikaa.submissionservice.dto.response.SubmissionStatusResponse;
import com.phenikaa.submissionservice.entity.ReportSubmission;
import com.phenikaa.submissionservice.exception.ReportSubmissionException;
import com.phenikaa.submissionservice.exception.SubmissionStatusException;
import com.phenikaa.submissionservice.repository.ReportSubmissionRepository;
import com.phenikaa.submissionservice.service.interfaces.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import com.phenikaa.submissionservice.dto.request.SubmissionFilterRequest;
@Service
@Slf4j
@Transactional
public class ReportSubmissionService {
    
    private final ReportSubmissionRepository reportSubmissionRepository;
    private final NotificationServiceClient communicationServiceClient;
    private final FileStorageService cloudinaryService;
    private final UserServiceClient userServiceClient;
    private final ThesisServiceClient thesisServiceClient;
    
    public ReportSubmissionService(
            ReportSubmissionRepository reportSubmissionRepository,
            NotificationServiceClient communicationServiceClient,
            @Qualifier("cloudinaryFileService") FileStorageService cloudinaryService,
            UserServiceClient userServiceClient,
            ThesisServiceClient thesisServiceClient
    ) {
        this.reportSubmissionRepository = reportSubmissionRepository;
        this.communicationServiceClient = communicationServiceClient;
        this.cloudinaryService = cloudinaryService;
        this.userServiceClient = userServiceClient;
        this.thesisServiceClient = thesisServiceClient;
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
            // Only update deadline if provided (for update operations, deadline might be null)
            if (request.getDeadline() != null) {
                submission.setDeadline(request.getDeadline());
            }
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
     * Tìm kiếm + lọc bằng native query
     */
    public Page<ReportSubmissionResponse> filterSubmissions(SubmissionFilterRequest req, Integer submittedBy) {
        // Sử dụng native query để tránh vấn đề với TEXT field
        List<ReportSubmission> submissions = reportSubmissionRepository.searchSubmissions(
                req.getSearch(), req.getSubmissionType(), submittedBy
        );
        
        // Manual pagination
        int page = req.getPage() == null ? 0 : req.getPage();
        int size = req.getSize() == null ? 10 : req.getSize();
        int start = page * size;
        int end = Math.min(start + size, submissions.size());
        
        List<ReportSubmission> pageContent = submissions.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(
                pageContent.stream().map(this::convertToResponse).toList(),
                PageRequest.of(page, size),
                submissions.size()
        );
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
     * Gửi thông báo qua communication-log-service cho giảng viên hướng dẫn
     */
    private void sendSubmissionNotification(ReportSubmission submission, String notificationType) {
        try {
            // Lấy thông tin topic để lấy supervisorId
            Map<String, Object> topicInfo = thesisServiceClient.getTopicById(submission.getTopicId());
            Integer supervisorId = (Integer) topicInfo.get("supervisorId");
            
            if (supervisorId == null) {
                log.warn("Could not find supervisorId for topic: {}", submission.getTopicId());
                return;
            }
            
            // Lấy tên sinh viên để hiển thị trong thông báo
            String studentName = "Sinh viên";
            try {
                GetUserResponse studentInfo = userServiceClient.getUserById(submission.getSubmittedBy());
                if (studentInfo != null && studentInfo.getFullName() != null) {
                    studentName = studentInfo.getFullName();
                }
            } catch (Exception e) {
                log.warn("Could not get student name for user: {}", submission.getSubmittedBy());
            }
            
            Map<String, Object> notification = Map.of(
                "type", notificationType,
                "receiverId", supervisorId, // Gửi cho giảng viên hướng dẫn
                "message", String.format("Sinh viên %s đã %s báo cáo '%s'", 
                    studentName,
                    notificationType.equals("SUBMISSION_CREATED") ? "nộp" : "cập nhật", 
                    submission.getReportTitle()),
                "data", Map.of(
                    "submissionId", submission.getSubmissionId(),
                    "topicId", submission.getTopicId(),
                    "reportTitle", submission.getReportTitle(),
                    "submittedBy", submission.getSubmittedBy(),
                    "studentName", studentName,
                    "submittedAt", submission.getSubmittedAt()
                )
            );
            
            communicationServiceClient.sendNotification(notification);
            log.info("Notification sent to supervisor {} for submission: {}", supervisorId, submission.getSubmissionId());
            
        } catch (Exception e) {
            log.error("Error sending notification for submission {}: {}", 
                submission.getSubmissionId(), e.getMessage());
        }
    }

    /**
     * Check submission status for thesis progress calculation
     */
    public SubmissionStatusResponse checkSubmissionStatus(Integer userId) {
        try {
            log.info("Checking submission status for user: {}", userId);
            
            // Get latest submission by user
            Optional<ReportSubmission> latestSubmission = 
                reportSubmissionRepository.findFirstBySubmittedByOrderBySubmittedAtDesc(userId);
            
            // Check milestone completion based on submissionType
            boolean softCopySubmitted = reportSubmissionRepository.existsBySubmittedByAndSubmissionType(userId, 2); // Báo cáo KLTN PDF
            boolean hardCopySubmitted = reportSubmissionRepository.existsBySubmittedByAndSubmissionType(userId, 3); // Bản cứng
            boolean finalCopySubmitted = reportSubmissionRepository.existsBySubmittedByAndSubmissionType(userId, 4); // Bìa đỏ
            
            // Defense completion - TODO: Currently hardcoded to false, integrate with eval-service to check actual defense status
            // This would require calling eval-service API to check if student has successfully completed defense session
            boolean defenseCompleted = false;
            
            // Calculate progress
            int completedMilestones = 0;
            if (softCopySubmitted) completedMilestones++;
            if (hardCopySubmitted) completedMilestones++;
            if (defenseCompleted) completedMilestones++;
            if (finalCopySubmitted) completedMilestones++;
            
            int totalMilestones = 4;
            int progressPercentage = Math.round((completedMilestones * 100.0f) / totalMilestones);
            
            // Get submission type descriptions
            String lastSubmissionTypeDesc = "";
            LocalDateTime lastSubmissionDate = null;
            Integer lastSubmissionType = null;
            Integer lastSubmittedAssignmentId = null;
            
            if (latestSubmission.isPresent()) {
                ReportSubmission latest = latestSubmission.get();
                lastSubmissionDate = latest.getSubmittedAt();
                lastSubmissionType = latest.getSubmissionType();
                lastSubmissionTypeDesc = getSubmissionTypeDescription(latest.getSubmissionType());
                lastSubmittedAssignmentId = latest.getAssignmentId();
            }
            
            // Build milestones detail
            List<SubmissionStatusResponse.MilestoneDetail> milestones = List.of(
                SubmissionStatusResponse.MilestoneDetail.builder()
                    .id("soft_copy_submission")
                    .name("Nộp bản mềm PDF")
                    .weight(25)
                    .completed(softCopySubmitted)
                    .completedAt(softCopySubmitted ? lastSubmissionDate : null)
                    .description("Báo cáo KLTN dưới dạng PDF")
                    .build(),
                SubmissionStatusResponse.MilestoneDetail.builder()
                    .id("hard_copy_submission")
                    .name("Nộp bản cứng")
                    .weight(25)
                    .completed(hardCopySubmitted)
                    .completedAt(hardCopySubmitted ? lastSubmissionDate : null)
                    .description("Bản cứng của đồ án")
                    .build(),
                SubmissionStatusResponse.MilestoneDetail.builder()
                    .id("thesis_defense")
                    .name("Bảo vệ luận văn")
                    .weight(30)
                    .completed(defenseCompleted)
                    .completedAt(defenseCompleted ? lastSubmissionDate : null)
                    .description("Tham gia bảo vệ đồ án")
                    .build(),
                SubmissionStatusResponse.MilestoneDetail.builder()
                    .id("final_hard_copy")
                    .name("Nộp bản cứng bìa đỏ")
                    .weight(20)
                    .completed(finalCopySubmitted)
                    .completedAt(finalCopySubmitted ? lastSubmissionDate : null)
                    .description("Bản cứng với bìa đỏ sau khi bảo vệ")
                    .build()
            );
            
            return SubmissionStatusResponse.builder()
                .userId(userId)
                .username("Student") // Default username
                .softCopySubmitted(softCopySubmitted)
                .hardCopySubmitted(hardCopySubmitted)
                .defenseCompleted(defenseCompleted)
                .finalCopySubmitted(finalCopySubmitted)
                .progressPercentage(progressPercentage)
                .completedMilestones(completedMilestones)
                .totalMilestones(totalMilestones)
                .lastSubmissionDate(lastSubmissionDate)
                .lastSubmissionType(lastSubmissionType)
                .lastSubmissionTypeDescription(lastSubmissionTypeDesc)
                .lastSubmittedAssignmentId(lastSubmittedAssignmentId)
                .milestones(milestones)
                .build();
                
        } catch (Exception e) {
            log.error("Error checking submission status for user {}: {}", userId, e.getMessage(), e);
            throw new SubmissionStatusException("Failed to check submission status for user: " + userId, e);
        }
    }
    
    /**
     * Get submission type description
     */
    private String getSubmissionTypeDescription(Integer submissionType) {
        return switch (submissionType) {
            case 1 -> "Báo cáo tiến độ";
            case 2 -> "Báo cáo KLTN (PDF)";
            case 3 -> "Bản cứng";
            case 4 -> "Bản cứng bìa đỏ";
            case 5 -> "Báo cáo đánh giá";
            default -> "Không xác định";
        };
    }
    
    /**
     * Convert entity to response DTO with populated studentName
     */
    private ReportSubmissionResponse convertToResponse(ReportSubmission submission) {
        if (submission == null) {
            return null;
        }
        
        String studentName = "Sinh viên " + submission.getSubmittedBy(); // Default fallback
        
        try {
            // Try to get fullName from user-service
            GetUserResponse userResponse = userServiceClient.getUserById(submission.getSubmittedBy());
            if (userResponse != null && userResponse.getFullName() != null && !userResponse.getFullName().trim().isEmpty()) {
                studentName = userResponse.getFullName();
            }
        } catch (Exception e) {
            log.warn("Could not fetch user info for userId {}: {}", submission.getSubmittedBy(), e.getMessage());
        }
        
        ReportSubmissionResponse response = ReportSubmissionResponse.builder()
                .submissionId(submission.getSubmissionId())
                .topicId(submission.getTopicId())
                .submittedBy(submission.getSubmittedBy())
                .reportTitle(submission.getReportTitle())
                .description(submission.getDescription())
                .filePath(submission.getFilePath())
                .submissionType(submission.getSubmissionType())
                .submittedAt(submission.getSubmittedAt())
                .status(submission.getStatus())
                .isFinal(submission.getIsFinal())
                .submissionTypeName(getSubmissionTypeDescription(submission.getSubmissionType()))
                .statusName(getSubmissionStatusName(submission.getStatus()))
                .fullName(studentName)
                .fileName(getFileNameFromPath(submission.getFilePath()))
                .build();
        
        return response;
    }
    
    /**
     * Get status name
     */
    private String getSubmissionStatusName(Integer status) {
        if (status == null) return "Không xác định";
        return switch (status) {
            case 1 -> "Đã nộp";
            case 2 -> "Đang xem xét";
            case 3 -> "Đã duyệt";
            case 4 -> "Từ chối";
            default -> "Không xác định";
        };
    }
    
    /**
     * Extract filename from file path
     */
    private String getFileNameFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) return null;
        
        int lastSlashIndex = filePath.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < filePath.length() - 1) {
            return filePath.substring(lastSlashIndex + 1);
        }
        return filePath;
    }
}
