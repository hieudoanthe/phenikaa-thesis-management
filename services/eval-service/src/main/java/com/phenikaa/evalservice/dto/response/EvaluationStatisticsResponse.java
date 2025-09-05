package com.phenikaa.evalservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationStatisticsResponse {
    
    // Tổng số đánh giá
    private Long totalEvaluations;
    
    // Số đánh giá theo loại
    private Map<String, Long> typeCounts;
    
    // Số đánh giá theo trạng thái
    private Map<String, Long> statusCounts;
    
    // Thống kê theo tháng
    private List<MonthlyEvaluationData> monthlyData;
    
    // Thống kê theo giảng viên
    private List<LecturerEvaluationData> lecturerData;
    
    // Tỷ lệ hoàn thành đánh giá
    private Double completionRate;
    
    // Thời gian đánh giá trung bình (ngày)
    private Double averageEvaluationTime;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyEvaluationData {
        private String month;
        private Long total;
        private Long supervisor;
        private Long reviewer;
        private Long committee;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LecturerEvaluationData {
        private Integer lecturerId;
        private String lecturerName;
        private Long evaluationCount;
        private Double averageScore;
        private String mostCommonType;
    }
}
