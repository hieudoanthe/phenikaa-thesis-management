package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.dto.response.FinalScoreResponse;
import com.phenikaa.evalservice.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eval-service/student/evaluations")
@RequiredArgsConstructor
@Slf4j
public class StudentEvaluationController {

    private final EvaluationService evaluationService;

    @GetMapping("/topic/{topicId}/final-score")
    public ResponseEntity<FinalScoreResponse> getFinalScore(@PathVariable Integer topicId) {
        FinalScoreResponse finalScore = evaluationService.calculateFinalScore(topicId);
        if (finalScore == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(finalScore);
    }
}


