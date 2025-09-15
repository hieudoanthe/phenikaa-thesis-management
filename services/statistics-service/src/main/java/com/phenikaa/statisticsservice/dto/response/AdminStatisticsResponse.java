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
public class AdminStatisticsResponse {
    
    // Thống kê tổng quan
    private Long totalUsers;
    private Long totalStudents;
    private Long totalTeachers;
    private Long totalAdmins;
    private Long totalTopics;
    private Long totalRegistrations;
    private Long totalSubmissions;
    private Long totalEvaluations;
    private Long totalDefenseSessions;
    
    // Thống kê theo trạng thái
    private Map<String, Long> usersByStatus;
    private Map<String, Long> topicsByStatus;
    private Map<String, Long> registrationsByStatus;
    private Map<String, Long> submissionsByStatus;
    private Map<String, Long> evaluationsByStatus;
    private Map<String, Long> defenseSessionsByStatus;
    
    // Thống kê theo năm học
    private Map<String, Long> topicsByAcademicYear;
    private Map<String, Long> registrationsByAcademicYear;
    private Map<String, Long> submissionsByAcademicYear;
    
    // Thống kê theo chuyên ngành
    private Map<String, Long> topicsByMajor;
    private Map<String, Long> studentsByMajor;
    
    // Thống kê theo mức độ khó
    private Map<String, Long> topicsByDifficulty;
    
    // Thống kê điểm số
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private Map<String, Long> scoreDistribution;
    private Double passRate;
    
    // Thống kê theo thời gian
    private List<TimeSeriesData> registrationsOverTime;
    private List<TimeSeriesData> submissionsOverTime;
    private List<TimeSeriesData> evaluationsOverTime;
    
    // Thống kê top
    private List<TopTeacherData> topTeachersByTopicCount;
    private List<TopStudentData> topStudentsByScore;
    private List<TopTopicData> topTopicsByRegistration;
    
    // Thống kê deadline
    private Long onTimeSubmissions;
    private Long lateSubmissions;
    private Long overdueSubmissions;
    
    // Thống kê real-time
    private Long activeUsersToday;
    private Long newRegistrationsToday;
    private Long newSubmissionsToday;
    private Long pendingEvaluations;
    
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
    public static class TopTeacherData {
        private Integer teacherId;
        private String teacherName;
        private Long topicCount;
        private Long studentCount;
        private Double averageScore;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopStudentData {
        private Integer studentId;
        private String studentName;
        private String studentMajor;
        private Double averageScore;
        private Integer topicCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopTopicData {
        private Integer topicId;
        private String topicTitle;
        private String supervisorName;
        private Long registrationCount;
        private Double averageScore;
        private String difficultyLevel;
    }
}
