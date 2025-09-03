package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.dto.request.QnARequest;
import com.phenikaa.evalservice.dto.response.QnAResponse;
import com.phenikaa.evalservice.entity.DefenseQnA;
import com.phenikaa.evalservice.repository.DefenseQnARepository;
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
    
    /**
     * Thêm câu hỏi mới
     */
    public QnAResponse addQuestion(QnARequest request) {
        log.info("Adding question for topic: {}, student: {}, questioner: {}", 
                request.getTopicId(), request.getStudentId(), request.getQuestionerId());
        
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
     * Cập nhật câu trả lời
     */
    public QnAResponse updateAnswer(Integer qnaId, String answer) {
        log.info("Updating answer for QnA: {}", qnaId);
        
        var qnA = qnARepository.findById(qnaId);
        if (qnA.isEmpty()) {
            throw new RuntimeException("QnA not found with id: " + qnaId);
        }
        
        DefenseQnA existingQnA = qnA.get();
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
