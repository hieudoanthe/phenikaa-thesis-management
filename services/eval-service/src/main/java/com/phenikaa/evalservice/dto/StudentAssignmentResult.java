package com.phenikaa.evalservice.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class StudentAssignmentResult {
    
    private Boolean success = false;
    private String message;
    private Integer totalStudents;
    private Integer totalSessions;
    
    // Chi tiết phân chia
    private Map<Integer, List<StudentSessionAssignment>> sessionAssignments;
    
    // Thống kê
    private Map<String, Integer> studentsByMajor;
    private Map<Integer, Integer> studentsBySession;
    
    // Lỗi và cảnh báo
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    
    // Inner class cho thông tin phân chia
    @Data
    public static class StudentSessionAssignment {
        private Integer studentId;
        private String studentName;
        private String major;
        private Integer topicId;
        private String topicTitle;
        private Integer sessionId;
        private String sessionName;
        private Integer defenseOrder;
        private String defenseTime;
    }
}
