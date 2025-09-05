package com.phenikaa.evalservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsOverviewResponse {
    
    // Tổng số buổi bảo vệ
    private Long totalDefenseSessions;
    
    // Số buổi bảo vệ theo trạng thái
    private Long scheduledSessions;
    private Long inProgressSessions;
    private Long completedSessions;
    private Long cancelledSessions;
    
    // Tổng số sinh viên bảo vệ
    private Long totalStudents;
    
    // Số sinh viên theo trạng thái
    private Long pendingStudents;
    private Long completedStudents;
    private Long failedStudents;
    
    // Tổng số đánh giá
    private Long totalEvaluations;
    
    // Số đánh giá theo loại
    private Long supervisorEvaluations;
    private Long reviewerEvaluations;
    private Long committeeEvaluations;
    
    // Tổng số Q&A
    private Long totalQnAs;
    
    // Điểm trung bình
    private Double averageScore;
    
    // Tỷ lệ hoàn thành
    private Double completionRate;
}
