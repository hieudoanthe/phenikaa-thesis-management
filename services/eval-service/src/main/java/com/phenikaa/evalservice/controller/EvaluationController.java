package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.dto.request.EvaluationRequest;
import com.phenikaa.evalservice.dto.response.EvaluationResponse;
import com.phenikaa.evalservice.dto.response.FinalScoreResponse;
import com.phenikaa.evalservice.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
@Slf4j
public class EvaluationController {
    
    private final EvaluationService evaluationService;
    
    /**
     * Chấm điểm cho sinh viên
     */
    @PostMapping("/submit")
    public ResponseEntity<EvaluationResponse> submitEvaluation(@Valid @RequestBody EvaluationRequest request) {
        log.info("Received evaluation submission request: {}", request);
        
        try {
            EvaluationResponse response = evaluationService.submitEvaluation(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error submitting evaluation: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy tất cả đánh giá của một topic
     */
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<EvaluationResponse>> getEvaluationsByTopic(@PathVariable Integer topicId) {
        List<EvaluationResponse> evaluations = evaluationService.getEvaluationsByTopic(topicId);
        return ResponseEntity.ok(evaluations);
    }
    
    /**
     * Lấy đánh giá theo sinh viên
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EvaluationResponse>> getEvaluationsByStudent(@PathVariable Integer studentId) {
        List<EvaluationResponse> evaluations = evaluationService.getEvaluationsByStudent(studentId);
        return ResponseEntity.ok(evaluations);
    }
    
    /**
     * Lấy đánh giá theo giảng viên
     */
    @GetMapping("/evaluator/{evaluatorId}")
    public ResponseEntity<List<EvaluationResponse>> getEvaluationsByEvaluator(@PathVariable Integer evaluatorId) {
        List<EvaluationResponse> evaluations = evaluationService.getEvaluationsByEvaluator(evaluatorId);
        return ResponseEntity.ok(evaluations);
    }
    
    /**
     * Tính điểm trung bình cuối cùng: (GVHD x1 + GVPB x2 + HĐ x1) / 4
     */
    @GetMapping("/topic/{topicId}/final-score")
    public ResponseEntity<FinalScoreResponse> getFinalScore(@PathVariable Integer topicId) {
        FinalScoreResponse finalScore = evaluationService.calculateFinalScore(topicId);
        if (finalScore == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(finalScore);
    }
}
