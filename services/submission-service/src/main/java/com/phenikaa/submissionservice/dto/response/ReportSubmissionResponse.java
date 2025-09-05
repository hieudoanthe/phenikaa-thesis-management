package com.phenikaa.submissionservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSubmissionResponse {
    
    private Integer submissionId;
    private Integer topicId;
    private Integer submittedBy;
    private Integer assignmentId;
    private String reportTitle;
    private String description;
    private String filePath;
    private Integer submissionType;
    private LocalDateTime submittedAt;
    private LocalDate deadline;
    private Integer status;
    private Boolean isFinal;
    
    // Thông tin bổ sung
    private String submissionTypeName;
    private String statusName;
    private String studentName;
    private String topicTitle;
    private String fileName;
    private String fileSize;
    
    // Danh sách phản hồi
    private List<FeedbackResponse> feedbacks;
    
    // Thống kê
    private Integer feedbackCount;
    private Double averageScore;
    private Boolean hasFeedback;
    private Boolean isOverdue;
    
    // Helper methods để tạo tên hiển thị
    public String getSubmissionTypeName() {
        if (submissionType == null) return "Không xác định";
        return switch (submissionType) {
            case 1 -> "Báo cáo tiến độ";
            case 2 -> "Báo cáo cuối kỳ";
            case 3 -> "Báo cáo khác";
            default -> "Không xác định";
        };
    }
    
    public String getStatusName() {
        if (status == null) return "Không xác định";
        return switch (status) {
            case 1 -> "Đã nộp";
            case 2 -> "Đang xem xét";
            case 3 -> "Đã duyệt";
            case 4 -> "Từ chối";
            default -> "Không xác định";
        };
    }
    
    public Boolean getIsOverdue() {
        if (deadline == null || status == 3) return false;
        return LocalDate.now().isAfter(deadline);
    }
}
