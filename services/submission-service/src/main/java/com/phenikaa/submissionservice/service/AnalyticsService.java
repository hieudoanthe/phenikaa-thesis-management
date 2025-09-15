package com.phenikaa.submissionservice.service;

import com.phenikaa.submissionservice.dto.response.AnalyticsResponse;
import com.phenikaa.submissionservice.entity.Feedback;
import com.phenikaa.submissionservice.entity.ReportSubmission;
import com.phenikaa.submissionservice.repository.ReportSubmissionRepository;
import com.phenikaa.submissionservice.repository.FeedbackRepository;
import com.phenikaa.submissionservice.repository.SubmissionVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ReportSubmissionRepository reportSubmissionRepository;
    private final FeedbackRepository feedbackRepository;
    private final SubmissionVersionRepository submissionVersionRepository;

    /**
     * Lấy thống kê tổng quan
     */
    public AnalyticsResponse getOverallAnalytics() {
        try {
            log.info("Generating overall analytics");

            return AnalyticsResponse.builder()
                    .totalSubmissions(reportSubmissionRepository.count())
                    .totalFeedbacks(feedbackRepository.count())
                    .submissionsByStatus(getSubmissionsByStatus())
                    .feedbacksByType(getFeedbacksByType())
                    .submissionsOverTime(getSubmissionsOverTime())
                    .feedbacksOverTime(getFeedbacksOverTime())
                    .averageScore(calculateAverageScore())
                    .highestScore(getHighestScore())
                    .lowestScore(getLowestScore())
                    .scoreDistribution(getScoreDistribution())
                    .topSubmitters(getTopSubmitters())
                    .topReviewers(getTopReviewers())
                    .topTopics(getTopTopics())
                    .onTimeSubmissions(getOnTimeSubmissions())
                    .lateSubmissions(getLateSubmissions())
                    .overdueSubmissions(getOverdueSubmissions())
                    .totalVersions(submissionVersionRepository.count())
                    .averageVersionsPerSubmission(calculateAverageVersionsPerSubmission())
                    .versionDistribution(getVersionDistribution())
                    .build();

        } catch (Exception e) {
            log.error("Error generating overall analytics: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo thống kê: " + e.getMessage());
        }
    }

    /**
     * Lấy thống kê theo khoảng thời gian
     */
    public AnalyticsResponse getAnalyticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            log.info("Generating analytics for date range: {} to {}", startDate, endDate);

            return AnalyticsResponse.builder()
                    .totalSubmissions(reportSubmissionRepository.countBySubmittedAtBetween(startDate, endDate))
                    .totalFeedbacks(feedbackRepository.countByCreatedAtBetween(startDate, endDate))
                    .submissionsOverTime(getSubmissionsOverTime(startDate, endDate))
                    .feedbacksOverTime(getFeedbacksOverTime(startDate, endDate))
                    .averageScore(calculateAverageScore(startDate, endDate))
                    .build();

        } catch (Exception e) {
            log.error("Error generating date range analytics: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo thống kê theo khoảng thời gian: " + e.getMessage());
        }
    }

    /**
     * Lấy thống kê theo người dùng
     */
    public AnalyticsResponse getUserAnalytics(Integer userId) {
        try {
            log.info("Generating analytics for user: {}", userId);

            return AnalyticsResponse.builder()
                    .totalSubmissions(reportSubmissionRepository.countBySubmittedBy(userId))
                    .totalFeedbacks(feedbackRepository.countByReviewerId(userId))
                    .averageScore(calculateUserAverageScore(userId))
                    .build();

        } catch (Exception e) {
            log.error("Error generating user analytics: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo thống kê người dùng: " + e.getMessage());
        }
    }

    /**
     * Lấy thống kê theo đề tài
     */
    public AnalyticsResponse getTopicAnalytics(Integer topicId) {
        try {
            log.info("Generating analytics for topic: {}", topicId);

            return AnalyticsResponse.builder()
                    .totalSubmissions(reportSubmissionRepository.countByTopicId(topicId))
                    .averageScore(calculateTopicAverageScore(topicId))
                    .build();

        } catch (Exception e) {
            log.error("Error generating topic analytics: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo thống kê đề tài: " + e.getMessage());
        }
    }

    // Helper methods
    private Map<String, Long> getSubmissionsByStatus() {
        Map<String, Long> statusMap = new HashMap<>();
        statusMap.put("Đã nộp", reportSubmissionRepository.countByStatus(1));
        statusMap.put("Đang xem xét", reportSubmissionRepository.countByStatus(2));
        statusMap.put("Đã duyệt", reportSubmissionRepository.countByStatus(3));
        statusMap.put("Từ chối", reportSubmissionRepository.countByStatus(4));
        return statusMap;
    }

    private Map<String, Long> getFeedbacksByType() {
        Map<String, Long> typeMap = new HashMap<>();
        typeMap.put("Nhận xét chung", feedbackRepository.countByFeedbackType(1));
        typeMap.put("Đánh giá chi tiết", feedbackRepository.countByFeedbackType(2));
        typeMap.put("Yêu cầu sửa đổi", feedbackRepository.countByFeedbackType(3));
        return typeMap;
    }

    private List<AnalyticsResponse.TimeSeriesData> getSubmissionsOverTime() {
        // Implementation for submissions over time
        return new ArrayList<>();
    }

    private List<AnalyticsResponse.TimeSeriesData> getSubmissionsOverTime(LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for submissions over time in date range
        return new ArrayList<>();
    }

    private List<AnalyticsResponse.TimeSeriesData> getFeedbacksOverTime() {
        // Implementation for feedbacks over time
        return new ArrayList<>();
    }

    private List<AnalyticsResponse.TimeSeriesData> getFeedbacksOverTime(LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for feedbacks over time in date range
        return new ArrayList<>();
    }

    private Double calculateAverageScore() {
        try {
            return feedbackRepository.findFeedbacksWithScore().stream()
                    .mapToDouble(Feedback::getScore)
                    .average()
                    .orElse(0.0);
        } catch (Exception e) {
            log.warn("Error calculating average score: {}", e.getMessage());
            return 0.0;
        }
    }

    private Double calculateAverageScore(LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for calculating average score in date range
        return 0.0;
    }

    private Double calculateUserAverageScore(Integer userId) {
        // Implementation for calculating user average score
        return 0.0;
    }

    private Double calculateTopicAverageScore(Integer topicId) {
        // Implementation for calculating topic average score
        return 0.0;
    }

    private Double getHighestScore() {
        // Implementation for getting highest score
        return 0.0;
    }

    private Double getLowestScore() {
        // Implementation for getting lowest score
        return 0.0;
    }

    private Map<String, Long> getScoreDistribution() {
        // Implementation for score distribution
        return new HashMap<>();
    }

    private List<AnalyticsResponse.UserStats> getTopSubmitters() {
        // Implementation for top submitters
        return new ArrayList<>();
    }

    private List<AnalyticsResponse.UserStats> getTopReviewers() {
        // Implementation for top reviewers
        return new ArrayList<>();
    }

    private List<AnalyticsResponse.TopicStats> getTopTopics() {
        // Implementation for top topics
        return new ArrayList<>();
    }

    private Long getOnTimeSubmissions() {
        // Implementation for on-time submissions
        return 0L;
    }

    private Long getLateSubmissions() {
        // Implementation for late submissions
        return 0L;
    }

    private Long getOverdueSubmissions() {
        // Implementation for overdue submissions
        return 0L;
    }

    private Double calculateAverageVersionsPerSubmission() {
        // Implementation for average versions per submission
        return 0.0;
    }

    private List<AnalyticsResponse.VersionStats> getVersionDistribution() {
        // Implementation for version distribution
        return new ArrayList<>();
    }

    // Statistics methods for internal API
    public Long getSubmissionCount() {
        return reportSubmissionRepository.count();
    }

    public Long getSubmissionCountByStatus(Integer status) {
        return reportSubmissionRepository.countByStatus(status);
    }

    public Long getSubmissionCountByTopic(Integer topicId) {
        return reportSubmissionRepository.countByTopicId(topicId);
    }

    public Long getSubmissionCountByUser(Integer userId) {
        return reportSubmissionRepository.countBySubmittedBy(userId);
    }

    public List<Map<String, Object>> getSubmissionsOverTime(String startDate, String endDate) {
        // TODO: Implement submissions over time with date filtering
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getSubmissionsByTopic(Integer topicId) {
        // TODO: Implement submissions by topic
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getSubmissionsByUser(Integer userId) {
        // TODO: Implement submissions by user
        return new ArrayList<>();
    }

    public Map<String, Long> getDeadlineStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("onTime", getOnTimeSubmissions());
        stats.put("late", getLateSubmissions());
        stats.put("overdue", getOverdueSubmissions());
        return stats;
    }

    public Long getSubmissionsToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return reportSubmissionRepository.countBySubmittedAtBetween(startOfDay, endOfDay);
    }
    
    public List<Map<String, Object>> getTodaySubmissions() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        List<ReportSubmission> todaySubmissions = reportSubmissionRepository.findBySubmittedAtBetween(startOfDay, endOfDay);
        
        return todaySubmissions.stream()
                .map(submission -> {
                    Map<String, Object> submissionData = new HashMap<>();
                    submissionData.put("id", submission.getSubmissionId());
                    submissionData.put("studentId", submission.getSubmittedBy());
                    submissionData.put("topicId", submission.getTopicId());
                    submissionData.put("title", submission.getReportTitle());
                    submissionData.put("status", submission.getStatus());
                    submissionData.put("submittedAt", submission.getSubmittedAt());
                    return submissionData;
                })
                .collect(Collectors.toList());
    }
}