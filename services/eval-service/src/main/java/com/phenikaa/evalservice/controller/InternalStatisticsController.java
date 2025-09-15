package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/evaluations")
@RequiredArgsConstructor
@Slf4j
public class InternalStatisticsController {
    
    private final StatisticsService statisticsService;
    
    @GetMapping("/get-evaluation-count")
    public Long getEvaluationCount() {
        log.info("Getting total evaluation count");
        return statisticsService.getEvaluationCount();
    }
    
    @GetMapping("/get-evaluation-count-by-type")
    public Long getEvaluationCountByType(@RequestParam String type) {
        log.info("Getting evaluation count by type: {}", type);
        return statisticsService.getEvaluationCountByType(type);
    }
    
    @GetMapping("/get-evaluation-count-by-status")
    public Long getEvaluationCountByStatus(@RequestParam String status) {
        log.info("Getting evaluation count by status: {}", status);
        return statisticsService.getEvaluationCountByStatus(status);
    }
    
    @GetMapping("/get-evaluation-count-by-evaluator")
    public Long getEvaluationCountByEvaluator(@RequestParam Integer evaluatorId) {
        log.info("Getting evaluation count by evaluator: {}", evaluatorId);
        return statisticsService.getEvaluationCountByEvaluator(evaluatorId);
    }
    
    @GetMapping("/get-evaluations-over-time")
    public List<Map<String, Object>> getEvaluationsOverTime(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("Getting evaluations over time from {} to {}", startDate, endDate);
        return statisticsService.getEvaluationsOverTime(startDate, endDate);
    }
    
    @GetMapping("/get-evaluations-by-evaluator")
    public List<Map<String, Object>> getEvaluationsByEvaluator(@RequestParam Integer evaluatorId) {
        log.info("Getting evaluations by evaluator: {}", evaluatorId);
        return statisticsService.getEvaluationsByEvaluator(evaluatorId);
    }
    
    @GetMapping("/get-evaluations-by-topic")
    public List<Map<String, Object>> getEvaluationsByTopic(@RequestParam Integer topicId) {
        log.info("Getting evaluations by topic: {}", topicId);
        return statisticsService.getEvaluationsByTopic(topicId);
    }
    
    @GetMapping("/get-evaluations-by-student")
    public List<Map<String, Object>> getEvaluationsByStudent(@RequestParam Integer studentId) {
        log.info("Getting evaluations by student: {}", studentId);
        return statisticsService.getEvaluationsByStudent(studentId);
    }
    
    @GetMapping("/get-score-statistics")
    public Map<String, Object> getScoreStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("Getting score statistics from {} to {}", startDate, endDate);
        return statisticsService.getScoreStatistics(startDate, endDate);
    }
    
    @GetMapping("/get-pending-evaluations")
    public Long getPendingEvaluations() {
        log.info("Getting pending evaluations count");
        return statisticsService.getPendingEvaluations();
    }
    
    @GetMapping("/get-pending-evaluations-by-evaluator")
    public Long getPendingEvaluationsByEvaluator(@RequestParam Integer evaluatorId) {
        log.info("Getting pending evaluations by evaluator: {}", evaluatorId);
        return statisticsService.getPendingEvaluationsByEvaluator(evaluatorId);
    }
    
    @GetMapping("/get-pending-evaluations-list")
    public List<Map<String, Object>> getPendingEvaluationsList() {
        log.info("Getting pending evaluations list");
        return statisticsService.getPendingEvaluationsList();
    }
}
