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
import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentAssignmentService {

    private final DefenseSessionRepository defenseSessionRepository;
    private final StudentDefenseRepository studentDefenseRepository;
    private final ThesisServiceClient thesisServiceClient;
    private final NotificationServiceClient notificationServiceClient;



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
}
