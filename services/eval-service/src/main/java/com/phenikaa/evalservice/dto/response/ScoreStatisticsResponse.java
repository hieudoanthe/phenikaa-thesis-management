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
public class ScoreStatisticsResponse {
    
    // Điểm trung bình tổng
    private Double overallAverage;
    
    // Điểm trung bình theo loại đánh giá
    private Map<String, Double> averageByType;
    
    // Phân bố điểm
    private Map<String, Long> scoreDistribution;
    
    // Thống kê theo tháng
    private List<MonthlyScoreData> monthlyData;
    
    // Top điểm cao nhất
    private List<TopScoreData> topScores;
    
    // Thống kê theo chuyên ngành
    private List<MajorScoreData> majorData;
    
    // Thống kê theo giảng viên
    private List<LecturerScoreData> lecturerData;
    
    // Tỷ lệ đạt/không đạt
    private Double passRate;
    
    // Điểm trung bình theo mức độ khó
    private Map<String, Double> averageByDifficulty;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyScoreData {
        private String month;
        private Double average;
        private Long count;
        private Double highest;
        private Double lowest;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopScoreData {
        private Integer topicId;
        private String topicTitle;
        private String studentName;
        private Double score;
        private String evaluationType;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorScoreData {
        private String major;
        private Double average;
        private Long count;
        private Double highest;
        private Double lowest;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LecturerScoreData {
        private Integer lecturerId;
        private String lecturerName;
        private Double averageScore;
        private Long evaluationCount;
        private String evaluationType;
    }
}
