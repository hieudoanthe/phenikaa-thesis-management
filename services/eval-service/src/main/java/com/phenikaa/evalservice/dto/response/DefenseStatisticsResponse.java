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
public class DefenseStatisticsResponse {
    
    // Thống kê theo trạng thái
    private Map<String, Long> statusCounts;
    
    // Thống kê theo tháng
    private List<MonthlyDefenseData> monthlyData;
    
    // Thống kê theo phòng
    private Map<String, Long> roomCounts;
    
    // Thống kê theo giảng viên
    private List<LecturerDefenseData> lecturerData;
    
    // Thống kê thời gian trung bình
    private Double averageDuration;
    
    // Số buổi bảo vệ trong ngày
    private Long todaySessions;
    
    // Số buổi bảo vệ trong tuần
    private Long weekSessions;
    
    // Số buổi bảo vệ trong tháng
    private Long monthSessions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyDefenseData {
        private String month;
        private Long count;
        private Long completed;
        private Long pending;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LecturerDefenseData {
        private Integer lecturerId;
        private String lecturerName;
        private Long sessionCount;
        private Long studentCount;
    }
}
