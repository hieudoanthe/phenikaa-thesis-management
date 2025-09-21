package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.dto.request.QnARequest;
import com.phenikaa.evalservice.dto.response.QnAResponse;
import com.phenikaa.evalservice.entity.DefenseQnA;
import com.phenikaa.evalservice.entity.DefenseCommittee;
import com.phenikaa.evalservice.entity.StudentDefense;
import com.phenikaa.evalservice.repository.DefenseQnARepository;
import com.phenikaa.evalservice.repository.DefenseCommitteeRepository;
import com.phenikaa.evalservice.repository.StudentDefenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DefenseQnAService {
    
    private final DefenseQnARepository qnARepository;
    private final DefenseCommitteeRepository defenseCommitteeRepository;
    private final StudentDefenseRepository studentDefenseRepository;
    
    /**
     * Thêm câu hỏi mới - chỉ cho phép thư ký
     */
    public QnAResponse addQuestion(QnARequest request) {
        log.info("Adding question for topic: {}, student: {}, questioner: {}, secretary: {}", 
                request.getTopicId(), request.getStudentId(), request.getQuestionerId(), request.getSecretaryId());
        
        // Kiểm tra quyền thư ký
        if (!isSecretaryForTopic(request.getSecretaryId(), request.getTopicId())) {
            throw new RuntimeException("Chỉ có thư ký mới được phép thêm câu hỏi Q&A");
        }
        
        DefenseQnA qnA = new DefenseQnA();
        qnA.setTopicId(request.getTopicId());
        qnA.setStudentId(request.getStudentId());
        qnA.setQuestionerId(request.getQuestionerId());
        qnA.setSecretaryId(request.getSecretaryId());
        qnA.setQuestion(request.getQuestion());
        qnA.setAnswer(request.getAnswer());
        qnA.setQuestionTime(LocalDateTime.now());
        
        if (request.getAnswer() != null && !request.getAnswer().trim().isEmpty()) {
            qnA.setAnswerTime(LocalDateTime.now());
        }
        
        DefenseQnA savedQnA = qnARepository.save(qnA);
        
        log.info("Question added successfully: {}", savedQnA.getQnaId());
        
        return convertToResponse(savedQnA);
    }
    
    /**
     * Cập nhật câu trả lời - chỉ cho phép thư ký
     */
    public QnAResponse updateAnswer(Integer qnaId, String answer, Integer secretaryId) {
        log.info("Updating answer for QnA: {} by secretary: {}", qnaId, secretaryId);
        
        var qnA = qnARepository.findById(qnaId);
        if (qnA.isEmpty()) {
            throw new RuntimeException("QnA not found with id: " + qnaId);
        }
        
        DefenseQnA existingQnA = qnA.get();
        
        // Kiểm tra quyền thư ký
        if (!isSecretaryForTopic(secretaryId, existingQnA.getTopicId())) {
            throw new RuntimeException("Chỉ có thư ký mới được phép cập nhật câu trả lời Q&A");
        }
        
        existingQnA.setAnswer(answer);
        existingQnA.setAnswerTime(LocalDateTime.now());
        
        DefenseQnA savedQnA = qnARepository.save(existingQnA);
        
        log.info("Answer updated successfully for QnA: {}", qnaId);
        
        return convertToResponse(savedQnA);
    }
    
    /**
     * Lấy tất cả Q&A của một topic
     */
    public List<QnAResponse> getQnAByTopic(Integer topicId) {
        List<DefenseQnA> qnAs = qnARepository.findByTopicIdOrderByQuestionTime(topicId);
        return qnAs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy Q&A theo sinh viên
     */
    public List<QnAResponse> getQnAByStudent(Integer studentId) {
        List<DefenseQnA> qnAs = qnARepository.findByStudentId(studentId);
        return qnAs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy Q&A theo topic và sinh viên
     */
    public List<QnAResponse> getQnAByTopicAndStudent(Integer topicId, Integer studentId) {
        List<DefenseQnA> qnAs = qnARepository.findByTopicIdAndStudentId(topicId, studentId);
        return qnAs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Kiểm tra xem người dùng có phải là thư ký trong buổi bảo vệ của topic này không
     */
    private boolean isSecretaryForTopic(Integer secretaryId, Integer topicId) {
        try {
            // Tìm StudentDefense theo topicId để lấy defense session
            var studentDefenseOpt = studentDefenseRepository.findByTopicId(topicId);
            if (studentDefenseOpt.isEmpty()) {
                log.warn("No StudentDefense found for topicId: {}", topicId);
                return false;
            }
            
            var studentDefense = studentDefenseOpt.get();
            var defenseSession = studentDefense.getDefenseSession();
            if (defenseSession == null) {
                log.warn("No DefenseSession found for topicId: {}", topicId);
                return false;
            }
            
            // Tìm DefenseCommittee với vai trò SECRETARY trong session này
            var secretaryCommittee = defenseCommitteeRepository.findByDefenseSession_SessionIdAndRole(
                defenseSession.getSessionId(), 
                DefenseCommittee.CommitteeRole.SECRETARY
            );
            
            boolean isSecretary = secretaryCommittee.isPresent() && 
                secretaryCommittee.get().getLecturerId().equals(secretaryId);
            
            log.info("Secretary check for secretaryId={}, topicId={}, sessionId={}, isSecretary={}", 
                secretaryId, topicId, defenseSession.getSessionId(), isSecretary);
            
            return isSecretary;
        } catch (Exception e) {
            log.error("Error checking secretary role for secretaryId={}, topicId={}: {}", 
                secretaryId, topicId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Kiểm tra quyền truy cập Q&A - chỉ cho phép thư ký
     */
    public boolean hasSecretaryAccess(Integer secretaryId, Integer topicId) {
        return isSecretaryForTopic(secretaryId, topicId);
    }

    /**
     * Kiểm tra quyền truy cập và trả về lý do nếu không có quyền
     */
    public String getNoAccessReason(Integer secretaryId, Integer topicId) {
        try {
            var studentDefenseOpt = studentDefenseRepository.findByTopicId(topicId);
            if (studentDefenseOpt.isEmpty()) {
                log.warn("No StudentDefense found for topicId: {}", topicId);
                return "NO_STUDENT_DEFENSE_FOR_TOPIC";
            }
            var studentDefense = studentDefenseOpt.get();
            var defenseSession = studentDefense.getDefenseSession();
            if (defenseSession == null) {
                log.warn("No DefenseSession found for topicId: {}", topicId);
                return "NO_DEFENSE_SESSION_FOR_TOPIC";
            }
            var secretaryCommittee = defenseCommitteeRepository.findByDefenseSession_SessionIdAndRole(
                defenseSession.getSessionId(),
                DefenseCommittee.CommitteeRole.SECRETARY
            );
            if (secretaryCommittee.isEmpty()) {
                return "NO_SECRETARY_ROLE_IN_SESSION";
            }
            boolean isMatch = secretaryCommittee.get().getLecturerId().equals(secretaryId);
            if (!isMatch) {
                return "SECRETARY_ROLE_MISMATCH";
            }
            return null; // Có quyền
        } catch (Exception e) {
            log.error("Error checking secretary access reason: secId={}, topicId={}, err={}", secretaryId, topicId, e.getMessage(), e);
            return "INTERNAL_ERROR";
        }
    }

    /**
     * Lấy danh sách thành viên hội đồng theo topic (suy ra session từ StudentDefense)
     */
    public java.util.List<DefenseCommittee> getCommitteeByTopic(Integer topicId) {
        var studentDefenseOpt = studentDefenseRepository.findByTopicId(topicId);
        if (studentDefenseOpt.isEmpty() || studentDefenseOpt.get().getDefenseSession() == null) {
            return java.util.Collections.emptyList();
        }
        Integer sessionId = studentDefenseOpt.get().getDefenseSession().getSessionId();
        return defenseCommitteeRepository.findByDefenseSession_SessionId(sessionId);
    }
    
    /**
     * Convert entity to response
     */
    private QnAResponse convertToResponse(DefenseQnA qnA) {
        QnAResponse response = new QnAResponse();
        response.setQnaId(qnA.getQnaId());
        response.setTopicId(qnA.getTopicId());
        response.setStudentId(qnA.getStudentId());
        response.setQuestionerId(qnA.getQuestionerId());
        response.setSecretaryId(qnA.getSecretaryId());
        response.setQuestion(qnA.getQuestion());
        response.setAnswer(qnA.getAnswer());
        response.setQuestionTime(qnA.getQuestionTime());
        response.setAnswerTime(qnA.getAnswerTime());
        
        // TODO: Lấy thông tin tên từ user-service và thesis-service
        // response.setStudentName(...);
        // response.setTopicTitle(...);
        // response.setQuestionerName(...);
        // response.setSecretaryName(...);
        
        return response;
    }
}
