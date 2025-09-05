package com.phenikaa.submissionservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {
    
    @NotNull(message = "Submission ID không được để trống")
    private Integer submissionId;
    
    @NotNull(message = "Reviewer ID không được để trống")
    private Integer reviewerId;
    
    @Size(max = 2000, message = "Nội dung phản hồi không được quá 2000 ký tự")
    private String content;
    
    private Float score; // Điểm số từ 0-10
    
    @NotNull(message = "Loại feedback không được để trống")
    private Integer feedbackType; // 1: Nhận xét chung, 2: Đánh giá chi tiết, 3: Yêu cầu sửa đổi
    
    private Boolean isApproved = false;
    
    // Constructor cho việc tạo mới
    public FeedbackRequest(Integer submissionId, Integer reviewerId, String content, 
                          Float score, Integer feedbackType) {
        this.submissionId = submissionId;
        this.reviewerId = reviewerId;
        this.content = content;
        this.score = score;
        this.feedbackType = feedbackType;
        this.isApproved = false;
    }
}
