package com.phenikaa.submissionservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {
    
    private Integer feedbackId;
    private Integer submissionId;
    private Integer reviewerId;
    private String content;
    private Float score;
    private Integer feedbackType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isApproved;
    
    // Thông tin bổ sung
    private String feedbackTypeName;
    private String reviewerName;
    private String submissionTitle;
    
    // Helper methods
    public String getFeedbackTypeName() {
        if (feedbackType == null) return "Không xác định";
        return switch (feedbackType) {
            case 1 -> "Nhận xét chung";
            case 2 -> "Đánh giá chi tiết";
            case 3 -> "Yêu cầu sửa đổi";
            default -> "Không xác định";
        };
    }
}
