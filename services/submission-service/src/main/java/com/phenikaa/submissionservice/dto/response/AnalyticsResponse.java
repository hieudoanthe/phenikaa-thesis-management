package com.phenikaa.submissionservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsResponse {
    
    // Thống kê tổng quan
    private Long totalSubmissions;
    private Long totalFeedbacks;
    private Long totalUsers;
    private Long totalTopics;
    
    // Thống kê theo trạng thái
    private Map<String, Long> submissionsByStatus;
    private Map<String, Long> feedbacksByType;
    
    // Thống kê theo thời gian
    private List<TimeSeriesData> submissionsOverTime;
    private List<TimeSeriesData> feedbacksOverTime;
    
    // Thống kê điểm số
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private Map<String, Long> scoreDistribution;
    
    // Thống kê theo người dùng
    private List<UserStats> topSubmitters;
    private List<UserStats> topReviewers;
    
    // Thống kê theo đề tài
    private List<TopicStats> topTopics;
    private List<TopicStats> topicsBySubmissionCount;
    
    // Thống kê deadline
    private Long onTimeSubmissions;
    private Long lateSubmissions;
    private Long overdueSubmissions;
    
    // Thống kê version control
    private Long totalVersions;
    private Double averageVersionsPerSubmission;
    private List<VersionStats> versionDistribution;
    
    // Thống kê email
    private Long totalEmailsSent;
    private Long emailsSentToday;
    private Map<String, Long> emailsByType;
    
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
    public static class UserStats {
        private Integer userId;
        private String userName;
        private Long count;
        private Double averageScore;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicStats {
        private Integer topicId;
        private String topicTitle;
        private Long submissionCount;
        private Long feedbackCount;
        private Double averageScore;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VersionStats {
        private Integer versionNumber;
        private Long count;
        private Double percentage;
    }
}
