package com.phenikaa.statisticsservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherStatisticsResponse {
    
    // Thống kê cá nhân
    private Integer teacherId;
    private String teacherName;
    private Long totalTopics;
    private Long totalStudents;
    private Long totalEvaluations;
    private Long totalDefenseSessions;
    
    // Thống kê đề tài của giảng viên
    private Map<String, Long> topicsByStatus;
    private Map<String, Long> topicsByDifficulty;
    private Map<String, Long> topicsByAcademicYear;
    
    // Thống kê sinh viên hướng dẫn
    private Map<String, Long> studentsByStatus;
    private Map<String, Long> studentsByMajor;
    private Map<String, Long> studentsByAcademicYear;
    
    // Thống kê đánh giá
    private Map<String, Long> evaluationsByType;
    private Map<String, Long> evaluationsByStatus;
    private Double averageEvaluationScore;
    private Long completedEvaluations;
    private Long pendingEvaluations;
    
    // Thống kê điểm số sinh viên
    private Double averageStudentScore;
    private Double highestStudentScore;
    private Double lowestStudentScore;
    private Map<String, Long> studentScoreDistribution;
    private Double studentPassRate;
    
    // Thống kê theo thời gian
    private List<TimeSeriesData> topicsCreatedOverTime;
    private List<TimeSeriesData> evaluationsOverTime;
    private List<TimeSeriesData> studentProgressOverTime;
    
    // Thống kê deadline
    private Long onTimeSubmissions;
    private Long lateSubmissions;
    private Long overdueSubmissions;
    
    // Thống kê workload
    private Long currentActiveTopics;
    private Long currentActiveStudents;
    private Long currentPendingEvaluations;
    private Double averageWorkloadPerMonth;
    
    // Thống kê performance
    private List<StudentPerformanceData> topPerformingStudents;
    private List<StudentPerformanceData> strugglingStudents;
    private List<TopicPerformanceData> topPerformingTopics;
    
    // Thống kê real-time
    private Long newSubmissionsToday;
    private Long evaluationsDueToday;
    private Long studentsNeedingAttention;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSeriesData {
        private String date;
        private Long count;
        private String type;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentPerformanceData {
        private Integer studentId;
        private String studentName;
        private String studentMajor;
        private Integer topicId;
        private String topicTitle;
        private Double currentScore;
        private String performanceStatus; // EXCELLENT, GOOD, AVERAGE, POOR
        private Long submissionCount;
        private Long evaluationCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicPerformanceData {
        private Integer topicId;
        private String topicTitle;
        private String difficultyLevel;
        private Long studentCount;
        private Double averageScore;
        private Long registrationCount;
        private String popularityStatus; // HIGH, MEDIUM, LOW
    }
}
