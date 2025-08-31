package com.phenikaa.evalservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentAssignmentRequest {
    
    private Integer scheduleId;
    private List<Integer> studentIds; // Danh sách ID sinh viên cần phân chia
    private Boolean autoAssign = true; // Tự động phân chia
    private String assignmentStrategy = "BY_MAJOR"; // Chiến lược phân chia: BY_MAJOR, ROUND_ROBIN, MANUAL
    
    // Các tùy chọn phân chia
    private Integer maxStudentsPerSession = 10; // Số sinh viên tối đa mỗi buổi
    private Boolean balanceByMajor = true; // Cân bằng theo chuyên ngành
    private Boolean considerLecturerAvailability = true; // Xem xét lịch giảng viên
}
