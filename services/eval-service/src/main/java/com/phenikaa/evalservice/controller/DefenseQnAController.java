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
import java.util.Map;

@RestController
@RequestMapping("/api/eval-service/teacher/defense-qna")
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
    public ResponseEntity<?> updateAnswer(@PathVariable Integer qnaId, @RequestBody Map<String, Object> request) {
        log.info("Updating answer for QnA: {}", qnaId);
        
        try {
            String answer = (String) request.get("answer");
            Integer secretaryId = (Integer) request.get("secretaryId");
            
            if (secretaryId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Secretary ID is required"));
            }
            
            QnAResponse response = qnAService.updateAnswer(qnaId, answer, secretaryId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error updating answer: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating answer: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
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
    
    /**
     * Kiểm tra quyền truy cập Q&A - chỉ cho phép thư ký
     */
    @GetMapping("/access/{topicId}/secretary/{secretaryId}")
    public ResponseEntity<?> checkSecretaryAccess(
            @PathVariable Integer topicId, 
            @PathVariable Integer secretaryId) {
        try {
            boolean hasAccess = qnAService.hasSecretaryAccess(secretaryId, topicId);
            String reason = hasAccess ? null : qnAService.getNoAccessReason(secretaryId, topicId);
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("hasAccess", hasAccess);
            if (reason != null) body.put("reason", reason);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("Error checking secretary access: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Lấy danh sách thành viên hội đồng theo topic
     */
    @GetMapping("/committee/topic/{topicId}")
    public ResponseEntity<?> getCommitteeByTopic(@PathVariable Integer topicId) {
        try {
            var list = qnAService.getCommitteeByTopic(topicId);
            // Trả về DTO gọn, tránh vòng lặp tuần hoàn khi serialize entity
            var result = list.stream().map(m -> {
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("committeeId", m.getCommitteeId());
                item.put("lecturerId", m.getLecturerId());
                item.put("role", m.getRole() != null ? m.getRole().name() : null);
                return item;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting committee by topic: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
}
