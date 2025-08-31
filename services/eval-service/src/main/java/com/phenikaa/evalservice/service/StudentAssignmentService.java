package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.dto.StudentAssignmentRequest;
import com.phenikaa.evalservice.dto.StudentAssignmentResult;
import com.phenikaa.evalservice.entity.DefenseSession;
import com.phenikaa.evalservice.entity.StudentDefense;
import com.phenikaa.evalservice.repository.DefenseSessionRepository;
import com.phenikaa.evalservice.repository.DefenseCommitteeRepository;
import com.phenikaa.evalservice.repository.StudentDefenseRepository;
import com.phenikaa.evalservice.client.ThesisServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentAssignmentService {

    private final DefenseSessionRepository defenseSessionRepository;
    private final StudentDefenseRepository studentDefenseRepository;
    
    // Inject các client để gọi profile-service và thesis-service
    private final ThesisServiceClient thesisServiceClient;

    /**
     * Phân chia sinh viên vào các buổi bảo vệ dựa trên chuyên ngành
     */
    public StudentAssignmentResult assignStudentsToSessions(StudentAssignmentRequest request) {
        log.info("Bắt đầu phân chia sinh viên cho schedule: {}", request.getScheduleId());
        
        StudentAssignmentResult result = new StudentAssignmentResult();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // 1. Lấy danh sách buổi bảo vệ
            List<DefenseSession> sessions = defenseSessionRepository
                    .findByDefenseSchedule_ScheduleId(request.getScheduleId());
            
            if (sessions.isEmpty()) {
                errors.add("Không tìm thấy buổi bảo vệ nào cho schedule này");
                result.setErrors(errors);
                return result;
            }
            
            // 2. Lấy danh sách sinh viên cần phân chia (từ thesis-service)
            List<StudentInfo> students = getStudentsForAssignment(request.getScheduleId());
            
            if (students.isEmpty()) {
                errors.add("Không tìm thấy sinh viên nào cần phân chia");
                result.setErrors(errors);
                return result;
            }
            
            // 3. Phân chia sinh viên theo chuyên ngành
            Map<String, List<StudentInfo>> studentsByMajor = students.stream()
                    .collect(Collectors.groupingBy(StudentInfo::getMajor));
            
            log.info("Phân chia sinh viên theo chuyên ngành: {}", studentsByMajor.keySet());
            
            // 4. Phân bổ sinh viên vào các buổi
            Map<Integer, List<StudentInfo>> sessionAssignments = new HashMap<>();
            
            for (DefenseSession session : sessions) {
                sessionAssignments.put(session.getSessionId(), new ArrayList<>());
            }
            
            // Phân chia theo thuật toán cân bằng
            distributeStudentsByMajor(studentsByMajor, sessions, sessionAssignments);
            
            // 5. Lưu kết quả phân chia
            saveStudentAssignments(sessionAssignments, request.getScheduleId());
            
            // 6. Cập nhật thống kê
            updateSessionStatistics(sessions);
            
            result.setSuccess(true);
            result.setMessage("Phân chia sinh viên thành công");
            result.setTotalStudents(students.size());
            result.setTotalSessions(sessions.size());
            
            log.info("Hoàn thành phân chia {} sinh viên vào {} buổi bảo vệ", 
                    students.size(), sessions.size());
            
        } catch (Exception e) {
            log.error("Lỗi khi phân chia sinh viên: ", e);
            errors.add("Lỗi hệ thống: " + e.getMessage());
            result.setErrors(errors);
        }
        
        result.setErrors(errors);
        result.setWarnings(warnings);
        return result;
    }

    /**
     * Phân chia sinh viên theo chuyên ngành một cách cân bằng
     */
    private void distributeStudentsByMajor(
            Map<String, List<StudentInfo>> studentsByMajor,
            List<DefenseSession> sessions,
            Map<Integer, List<StudentInfo>> sessionAssignments) {
        
        // Sắp xếp các buổi theo thứ tự ưu tiên (ngày, giờ)
        sessions.sort(Comparator.comparing(DefenseSession::getDefenseDate)
                .thenComparing(DefenseSession::getStartTime));
        
        // Phân chia từng chuyên ngành
        for (Map.Entry<String, List<StudentInfo>> entry : studentsByMajor.entrySet()) {
            String major = entry.getKey();
            List<StudentInfo> students = entry.getValue();
            
            log.info("Phân chia {} sinh viên chuyên ngành: {}", students.size(), major);
            
            // Tìm các buổi có giảng viên cùng chuyên ngành
            List<DefenseSession> compatibleSessions = findCompatibleSessions(sessions, major);
            
            if (compatibleSessions.isEmpty()) {
                log.warn("Không tìm thấy buổi nào có giảng viên chuyên ngành: {}", major);
                // Phân chia vào tất cả các buổi
                compatibleSessions = sessions;
            }
            
            // Phân chia sinh viên vào các buổi tương thích
            distributeStudentsToSessions(students, compatibleSessions, sessionAssignments);
        }
    }

    /**
     * Tìm các buổi có giảng viên cùng chuyên ngành
     */
    private List<DefenseSession> findCompatibleSessions(List<DefenseSession> sessions, String major) {
        return sessions.stream()
                .filter(session -> hasLecturerWithMajor(session, major))
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra buổi có giảng viên cùng chuyên ngành không
     */
    private boolean hasLecturerWithMajor(DefenseSession session, String major) {
        // TODO: Implement logic kiểm tra giảng viên trong hội đồng
        // Cần gọi profile-service để lấy thông tin giảng viên
        return true; // Tạm thời return true
    }

    /**
     * Phân chia sinh viên vào các buổi
     */
    private void distributeStudentsToSessions(
            List<StudentInfo> students,
            List<DefenseSession> sessions,
            Map<Integer, List<StudentInfo>> sessionAssignments) {
        
        int sessionIndex = 0;
        
        for (StudentInfo student : students) {
            // Tìm buổi có ít sinh viên nhất
            DefenseSession targetSession = findSessionWithLeastStudents(sessions, sessionAssignments);
            
            if (targetSession != null) {
                sessionAssignments.get(targetSession.getSessionId()).add(student);
                log.debug("Phân chia sinh viên {} vào buổi {}", 
                        student.getStudentName(), targetSession.getSessionName());
            }
            
            sessionIndex = (sessionIndex + 1) % sessions.size();
        }
    }

    /**
     * Tìm buổi có ít sinh viên nhất
     */
    private DefenseSession findSessionWithLeastStudents(
            List<DefenseSession> sessions,
            Map<Integer, List<StudentInfo>> sessionAssignments) {
        
        return sessions.stream()
                .min(Comparator.comparing(session -> 
                        sessionAssignments.get(session.getSessionId()).size()))
                .orElse(null);
    }

    /**
     * Lưu kết quả phân chia sinh viên
     */
    private void saveStudentAssignments(
            Map<Integer, List<StudentInfo>> sessionAssignments,
            Integer scheduleId) {
        
        for (Map.Entry<Integer, List<StudentInfo>> entry : sessionAssignments.entrySet()) {
            Integer sessionId = entry.getKey();
            List<StudentInfo> students = entry.getValue();
            
            DefenseSession session = defenseSessionRepository.findById(sessionId).orElse(null);
            if (session == null) continue;
            
            for (int i = 0; i < students.size(); i++) {
                StudentInfo student = students.get(i);
                
                StudentDefense studentDefense = StudentDefense.builder()
                        .defenseSession(session)
                        .studentId(student.getStudentId())
                        .topicId(student.getTopicId())
                        .supervisorId(student.getSupervisorId())
                        .studentName(student.getStudentName())
                        .studentMajor(student.getMajor())
                        .topicTitle(student.getTopicTitle())
                        .defenseOrder(i + 1)
                        .status(StudentDefense.DefenseStatus.SCHEDULED)
                        .build();
                
                studentDefenseRepository.save(studentDefense);
            }
        }
    }

    /**
     * Cập nhật thống kê cho các buổi
     */
    private void updateSessionStatistics(List<DefenseSession> sessions) {
        for (DefenseSession session : sessions) {
            long studentCount = studentDefenseRepository.countByDefenseSession_SessionId(session.getSessionId());
            
            if (studentCount > 0) {
                session.setStatus(DefenseSession.SessionStatus.SCHEDULED);
                defenseSessionRepository.save(session);
            }
        }
    }

    /**
     * Lấy thông tin sinh viên cần phân chia (từ thesis-service)
     * TODO: Implement gọi API thesis-service
     */
    private List<StudentInfo> getStudentsForAssignment(Integer scheduleId) {
        // Tạm thời return mock data
        List<StudentInfo> students = new ArrayList<>();
        
        // TODO: Gọi thesis-service để lấy danh sách sinh viên đã đăng ký đề tài
        // và đã được duyệt
        
        return students;
    }

    // Inner class để chứa thông tin sinh viên
    public static class StudentInfo {
        private Integer studentId;
        private String studentName;
        private String major;
        private Integer topicId;
        private String topicTitle;
        private Integer supervisorId;
        
        // Getters and setters
        public Integer getStudentId() { return studentId; }
        public void setStudentId(Integer studentId) { this.studentId = studentId; }
        
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        
        public String getMajor() { return major; }
        public void setMajor(String major) { this.major = major; }
        
        public Integer getTopicId() { return topicId; }
        public void setTopicId(Integer topicId) { this.topicId = topicId; }
        
        public String getTopicTitle() { return topicTitle; }
        public void setTopicTitle(String topicTitle) { this.topicTitle = topicTitle; }
        
        public Integer getSupervisorId() { return supervisorId; }
        public void setSupervisorId(Integer supervisorId) { this.supervisorId = supervisorId; }
    }
}
