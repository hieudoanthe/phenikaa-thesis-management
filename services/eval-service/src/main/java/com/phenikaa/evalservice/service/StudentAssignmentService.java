package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.client.NotificationServiceClient;
import com.phenikaa.evalservice.dto.StudentAssignmentRequest;
import com.phenikaa.evalservice.dto.StudentAssignmentResult;
import com.phenikaa.evalservice.entity.DefenseSession;
import com.phenikaa.evalservice.entity.StudentDefense;
import com.phenikaa.evalservice.repository.DefenseSessionRepository;
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
    private final NotificationServiceClient notificationServiceClient;

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
     * Gán sinh viên vào buổi bảo vệ cụ thể
     */
    public boolean assignStudentToSession(Integer sessionId, Integer studentId, Integer topicId, 
                                       Integer supervisorId, String studentName, String studentMajor, 
                                       String topicTitle) {
        try {
            // Kiểm tra buổi bảo vệ có tồn tại không
            DefenseSession session = defenseSessionRepository.findById(sessionId).orElse(null);
            if (session == null) {
                log.error("Không tìm thấy buổi bảo vệ với ID: {}", sessionId);
                return false;
            }

            // Kiểm tra số lượng sinh viên hiện tại
            long currentStudentCount = studentDefenseRepository.countByDefenseSession_SessionId(sessionId);
            if (currentStudentCount >= session.getMaxStudents()) {
                log.warn("Buổi bảo vệ {} đã đầy ({} sinh viên)", sessionId, currentStudentCount);
                return false;
            }

            // Kiểm tra sinh viên đã được gán vào buổi này chưa
            boolean alreadyAssigned = studentDefenseRepository
                .existsByDefenseSession_SessionIdAndStudentId(sessionId, studentId);
            if (alreadyAssigned) {
                log.warn("Sinh viên {} đã được gán vào buổi bảo vệ {}", studentId, sessionId);
                return false;
            }

            // Tạo assignment mới
            StudentDefense studentDefense = StudentDefense.builder()
                    .defenseSession(session)
                    .studentId(studentId)
                    .topicId(topicId)
                    .supervisorId(supervisorId)
                    .studentName(studentName)
                    .studentMajor(studentMajor)
                    .topicTitle(topicTitle)
                    .defenseOrder((int) currentStudentCount + 1)
                    .status(StudentDefense.DefenseStatus.SCHEDULED)
                    .build();

            studentDefenseRepository.save(studentDefense);
            
            // Cập nhật trạng thái buổi bảo vệ
            if (session.getStatus() == DefenseSession.SessionStatus.PLANNING) {
                session.setStatus(DefenseSession.SessionStatus.SCHEDULED);
                defenseSessionRepository.save(session);
            }

            // Gửi thông báo cho sinh viên
            sendAssignmentNotification(studentId, session, studentName, topicTitle);
            
            log.info("Gán sinh viên {} vào buổi bảo vệ {} thành công", studentId, sessionId);
            return true;
            
        } catch (Exception e) {
            log.error("Lỗi khi gán sinh viên {} vào buổi bảo vệ {}: ", studentId, sessionId, e);
            return false;
        }
    }

    /**
     * Hủy gán sinh viên khỏi buổi bảo vệ
     */
    public boolean unassignStudentFromSession(Integer sessionId, Integer studentId) {
        try {
            StudentDefense assignment = studentDefenseRepository
                .findByDefenseSession_SessionIdAndStudentId(sessionId, studentId)
                .orElse(null);
            
            if (assignment == null) {
                log.warn("Không tìm thấy assignment của sinh viên {} trong buổi bảo vệ {}", studentId, sessionId);
                return false;
            }

            // Xóa assignment
            studentDefenseRepository.delete(assignment);
            
            // Cập nhật thứ tự bảo vệ cho các sinh viên còn lại
            updateDefenseOrder(sessionId);
            
            // Kiểm tra nếu không còn sinh viên nào thì chuyển về PLANNING
            long remainingStudents = studentDefenseRepository.countByDefenseSession_SessionId(sessionId);
            if (remainingStudents == 0) {
                DefenseSession session = defenseSessionRepository.findById(sessionId).orElse(null);
                if (session != null) {
                    session.setStatus(DefenseSession.SessionStatus.PLANNING);
                    defenseSessionRepository.save(session);
                }
            }
            
            // Gửi thông báo cho sinh viên về việc hủy gán
            DefenseSession sessionForNotification = defenseSessionRepository.findById(sessionId).orElse(null);
            if (sessionForNotification != null) {
                sendUnassignmentNotification(studentId, sessionForNotification, assignment.getStudentName(), assignment.getTopicTitle());
            }

            log.info("Hủy gán sinh viên {} khỏi buổi bảo vệ {} thành công", studentId, sessionId);
            return true;
            
        } catch (Exception e) {
            log.error("Lỗi khi hủy gán sinh viên {} khỏi buổi bảo vệ {}: ", studentId, sessionId, e);
            return false;
        }
    }

    /**
     * Gửi thông báo cho sinh viên khi được gán vào buổi bảo vệ
     */
    private void sendAssignmentNotification(Integer studentId, DefenseSession session, String studentName, String topicTitle) {
        try {
            // Tạo nội dung thông báo
            String message = String.format(
                "Bạn đã được gán vào buổi bảo vệ đề tài '%s' vào ngày %s tại %s. Vui lòng chuẩn bị và có mặt đúng giờ.",
                topicTitle,
                session.getDefenseDate().toString(),
                session.getLocation()
            );

            // Tạo request body cho notification
            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("senderId", 0); // 0 = System
            notificationRequest.put("receiverId", studentId);
            notificationRequest.put("message", message);
            notificationRequest.put("type", "IMPORTANT");

            // Gửi thông báo qua communication-service
            try {
                notificationServiceClient.sendNotification(notificationRequest);
                log.info("Đã gửi thông báo cho sinh viên {} thành công", studentId);
            } catch (Exception e) {
                log.error("Lỗi khi gửi thông báo cho sinh viên {}: ", studentId, e);
                // Không throw exception để không ảnh hưởng đến logic chính
            }
        } catch (Exception e) {
            log.error("Lỗi khi tạo thông báo cho sinh viên {}: ", studentId, e);
        }
    }

    /**
     * Gửi thông báo cho sinh viên khi bị hủy gán khỏi buổi bảo vệ
     */
    private void sendUnassignmentNotification(Integer studentId, DefenseSession session, String studentName, String topicTitle) {
        try {
            // Tạo nội dung thông báo
            String message = String.format(
                "Bạn đã bị hủy gán khỏi buổi bảo vệ đề tài '%s' vào ngày %s tại %s. Vui lòng liên hệ với giảng viên hướng dẫn để biết thêm thông tin.",
                topicTitle,
                session.getDefenseDate().toString(),
                session.getLocation()
            );

            // Tạo request body cho notification
            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("senderId", 0); // 0 = System
            notificationRequest.put("receiverId", studentId);
            notificationRequest.put("message", message);
            notificationRequest.put("type", "UNASSIGNMENT"); // ✅ Set type để gửi email

            // Gửi thông báo qua communication-service
            try {
                notificationServiceClient.sendNotification(notificationRequest);
                log.info("Đã gửi thông báo hủy gán cho sinh viên {} thành công", studentId);
            } catch (Exception e) {
                log.error("Lỗi khi gửi thông báo hủy gán cho sinh viên {}: ", studentId, e);
                // Không throw exception để không ảnh hưởng đến logic chính
            }
        } catch (Exception e) {
            log.error("Lỗi khi tạo thông báo hủy gán cho sinh viên {}: ", studentId, e);
        }
    }

    /**
     * Cập nhật thứ tự bảo vệ sau khi xóa sinh viên
     */
    private void updateDefenseOrder(Integer sessionId) {
        List<StudentDefense> assignments = studentDefenseRepository
            .findByDefenseSession_SessionIdOrderByDefenseOrder(sessionId);
        
        for (int i = 0; i < assignments.size(); i++) {
            StudentDefense assignment = assignments.get(i);
            assignment.setDefenseOrder(i + 1);
            studentDefenseRepository.save(assignment);
        }
    }

    /**
     * Lấy danh sách sinh viên đã được gán vào buổi bảo vệ
     */
    public List<StudentDefense> getAssignedStudents(Integer sessionId) {
        try {
            return studentDefenseRepository.findByDefenseSession_SessionIdOrderByDefenseOrder(sessionId);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sinh viên đã gán cho buổi bảo vệ {}: ", sessionId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Lấy danh sách buổi bảo vệ có thể gán thêm sinh viên
     */
    public List<DefenseSession> getAvailableSessions() {
        try {
            List<DefenseSession> allSessions = defenseSessionRepository.findAll();
            List<DefenseSession> availableSessions = new ArrayList<>();
            
            for (DefenseSession session : allSessions) {
                long currentCount = studentDefenseRepository.countByDefenseSession_SessionId(session.getSessionId());
                if (currentCount < session.getMaxStudents()) {
                    availableSessions.add(session);
                }
            }
            
            return availableSessions;
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách buổi bảo vệ có sẵn: ", e);
            return new ArrayList<>();
        }
    }

    /**
     * Lấy thông tin sinh viên cần phân chia (từ thesis-service)
     * Lấy tất cả sinh viên từ đợt đăng ký hiện tại
     */
    private List<StudentInfo> getStudentsForAssignment(Integer scheduleId) {
        try {
            // Lấy đợt đăng ký hiện tại
            Map<String, Object> currentPeriod = thesisServiceClient.getCurrentPeriod();
            if (currentPeriod == null || currentPeriod.get("periodId") == null) {
                log.warn("Không tìm thấy đợt đăng ký hiện tại");
                return new ArrayList<>();
            }
            
            Integer periodId = (Integer) currentPeriod.get("periodId");
            log.info("Lấy sinh viên từ đợt đăng ký: {}", periodId);
            
            // Lấy tất cả sinh viên (đăng ký + đề xuất) từ đợt này
            List<Map<String, Object>> allStudents = thesisServiceClient.getAllStudentsByPeriod(periodId.toString());
            List<StudentInfo> students = new ArrayList<>();
            
            for (Map<String, Object> studentData : allStudents) {
                StudentInfo student = new StudentInfo();
                student.setStudentId((Integer) studentData.get("studentId"));
                student.setTopicId((Integer) studentData.get("topicId"));
                student.setTopicTitle((String) studentData.get("topicTitle"));
                student.setSupervisorId((Integer) studentData.get("supervisorId"));
                
                // Lấy thông tin chi tiết từ đề tài
                if (studentData.get("topicId") != null) {
                    try {
                        Map<String, Object> topicInfo = thesisServiceClient.getTopicById((Integer) studentData.get("topicId"));
                        if (topicInfo != null) {
                            student.setTopicTitle((String) topicInfo.get("title"));
                        }
                    } catch (Exception e) {
                        log.warn("Không thể lấy thông tin đề tài {}: {}", studentData.get("topicId"), e.getMessage());
                    }
                }
                
                // Tạm thời set tên và chuyên ngành mặc định
                // TODO: Cần gọi profile-service để lấy thông tin chi tiết
                student.setStudentName("Sinh viên " + student.getStudentId());
                student.setMajor("CNTT");
                
                students.add(student);
            }
            
            log.info("Lấy được {} sinh viên từ đợt đăng ký {}", students.size(), periodId);
            return students;
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sinh viên từ thesis-service: ", e);
            return new ArrayList<>();
        }
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
