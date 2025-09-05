package com.phenikaa.submissionservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportSubmissionRequest {
    
    @NotNull(message = "Topic ID không được để trống")
    private Integer topicId;
    
    @NotNull(message = "Submitted by không được để trống")
    private Integer submittedBy;
    
    private Integer assignmentId;
    
    @NotBlank(message = "Tiêu đề báo cáo không được để trống")
    @Size(max = 500, message = "Tiêu đề báo cáo không được quá 500 ký tự")
    private String reportTitle;
    
    @Size(max = 2000, message = "Mô tả không được quá 2000 ký tự")
    private String description;
    
    private String filePath;
    
    @NotNull(message = "Loại submission không được để trống")
    private Integer submissionType; // 1: Báo cáo tiến độ, 2: Báo cáo cuối kỳ, 3: Báo cáo khác
    
    private LocalDate deadline;
    
    private Integer status = 1; // 1: Đã nộp, 2: Đang xem xét, 3: Đã duyệt, 4: Từ chối
    
    private Boolean isFinal = false;
    
    // Constructor cho việc tạo mới
    public ReportSubmissionRequest(Integer topicId, Integer submittedBy, String reportTitle, 
                                 String description, Integer submissionType, LocalDate deadline) {
        this.topicId = topicId;
        this.submittedBy = submittedBy;
        this.reportTitle = reportTitle;
        this.description = description;
        this.submissionType = submissionType;
        this.deadline = deadline;
        this.status = 1;
        this.isFinal = false;
    }
}
