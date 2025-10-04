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
    private String reportTitle;
    private String description;
    private String filePath;
    private Integer submissionType;
    private LocalDateTime submittedAt;
    private Integer status;
    private Boolean isFinal;
    
    // Thông tin bổ sung
    private String submissionTypeName;
    private String statusName;
    private String fullName;
    private String fileName;
    
    // Danh sách phản hồi
    private List<FeedbackResponse> feedbacks;

}
