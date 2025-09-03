package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.dto.request.QnARequest;
import com.phenikaa.evalservice.dto.response.QnAResponse;
import com.phenikaa.evalservice.service.DefenseQnAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/defense-qna")
@RequiredArgsConstructor
@Slf4j
public class DefenseQnAController {
    
    private final DefenseQnAService qnAService;
    
    /**
     * Thêm câu hỏi mới
     */
    @PostMapping("/question")
    public ResponseEntity<QnAResponse> addQuestion(@Valid @RequestBody QnARequest request) {
        log.info("Received question request: {}", request);
        
        try {
            QnAResponse response = qnAService.addQuestion(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding question: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Cập nhật câu trả lời
     */
    @PutMapping("/{qnaId}/answer")
    public ResponseEntity<QnAResponse> updateAnswer(@PathVariable Integer qnaId, @RequestBody String answer) {
        log.info("Updating answer for QnA: {}", qnaId);
        
        try {
            QnAResponse response = qnAService.updateAnswer(qnaId, answer);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating answer: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy tất cả Q&A của một topic
     */
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<QnAResponse>> getQnAByTopic(@PathVariable Integer topicId) {
        List<QnAResponse> qnAs = qnAService.getQnAByTopic(topicId);
        return ResponseEntity.ok(qnAs);
    }
    
    /**
     * Lấy Q&A theo sinh viên
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<QnAResponse>> getQnAByStudent(@PathVariable Integer studentId) {
        List<QnAResponse> qnAs = qnAService.getQnAByStudent(studentId);
        return ResponseEntity.ok(qnAs);
    }
    
    /**
     * Lấy Q&A theo topic và sinh viên
     */
    @GetMapping("/topic/{topicId}/student/{studentId}")
    public ResponseEntity<List<QnAResponse>> getQnAByTopicAndStudent(
            @PathVariable Integer topicId, 
            @PathVariable Integer studentId) {
        List<QnAResponse> qnAs = qnAService.getQnAByTopicAndStudent(topicId, studentId);
        return ResponseEntity.ok(qnAs);
    }
}
