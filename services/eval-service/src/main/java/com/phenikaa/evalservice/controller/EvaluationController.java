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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/eval-service/teacher/evaluations")
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
     * Lấy danh sách nhiệm vụ chấm điểm theo giảng viên cho ngày bảo vệ (query param date=yyyy-MM-dd, mặc định hôm nay)
     */
    @GetMapping("/evaluator/{evaluatorId}/tasks")
    public ResponseEntity<List<EvaluationResponse>> getEvaluatorTasks(
            @PathVariable Integer evaluatorId,
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "scope", required = false, defaultValue = "today") String scope
    ) {
        LocalDate date;
        if ("all".equalsIgnoreCase(scope)) {
            date = null; // service sẽ hiểu null là lấy tất cả
        } else if (dateStr != null && !dateStr.isBlank()) {
            date = LocalDate.parse(dateStr);
        } else {
            date = LocalDate.now();
        }
        List<EvaluationResponse> tasks = evaluationService.getEvaluatorTasks(evaluatorId, date, scope);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Tính điểm trung bình cuối cùng: (GVHD x1 + GVPB x2 + HĐ x1) / 4
     * Trong đó HĐ chỉ được tính khi có đủ 3 thành viên hội đồng chấm điểm
     */
    @GetMapping("/topic/{topicId}/final-score")
    public ResponseEntity<FinalScoreResponse> getFinalScore(@PathVariable Integer topicId) {
        FinalScoreResponse finalScore = evaluationService.calculateFinalScore(topicId);
        if (finalScore == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(finalScore);
    }
    
    /**
     * Cập nhật trạng thái defense committee từ INVITED sang CONFIRMED
     */
    @PutMapping("/defense-committee/{committeeId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmDefenseCommittee(@PathVariable Integer committeeId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var committee = evaluationService.getDefenseCommitteeById(committeeId);
            if (committee.isEmpty()) {
                result.put("error", "Defense committee not found");
                return ResponseEntity.notFound().build();
            }
            
            var updatedCommittee = evaluationService.confirmDefenseCommittee(committeeId);
            result.put("success", true);
            result.put("committee", updatedCommittee);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("success", false);
        }
        
        return ResponseEntity.ok(result);
    }
    
    
    
    /**
     * Lấy thông tin chi tiết đề tài cho giảng viên chấm điểm
     */
    @GetMapping("/topic/{topicId}/details")
    public ResponseEntity<Map<String, Object>> getTopicDetails(@PathVariable Integer topicId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var topicDetails = evaluationService.getTopicDetailsForGrading(topicId);
            result.put("success", true);
            result.put("data", topicDetails);
        } catch (Exception e) {
            log.error("Error getting topic details for topicId {}: {}", topicId, e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    
}
