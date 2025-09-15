package com.phenikaa.statisticsservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "api-gateway",
        contextId = "evalServiceClient",
        path = "/eval-service",
        configuration = FeignTokenInterceptor.class
)
public interface EvalServiceClient {
    
    @GetMapping("/internal/evaluations/get-evaluation-count")
    Long getEvaluationCount();
    
    @GetMapping("/internal/evaluations/get-evaluation-count-by-type")
    Long getEvaluationCountByType(@RequestParam String type);
    
    @GetMapping("/internal/evaluations/get-evaluation-count-by-status")
    Long getEvaluationCountByStatus(@RequestParam String status);
    
    @GetMapping("/internal/evaluations/get-evaluation-count-by-evaluator")
    Long getEvaluationCountByEvaluator(@RequestParam Integer evaluatorId);
    
    @GetMapping("/internal/evaluations/get-evaluations-over-time")
    List<Map<String, Object>> getEvaluationsOverTime(@RequestParam(required = false) String startDate, 
                                                    @RequestParam(required = false) String endDate);
    
    @GetMapping("/internal/evaluations/get-evaluations-by-evaluator")
    List<Map<String, Object>> getEvaluationsByEvaluator(@RequestParam Integer evaluatorId);
    
    @GetMapping("/internal/evaluations/get-evaluations-by-topic")
    List<Map<String, Object>> getEvaluationsByTopic(@RequestParam Integer topicId);
    
    @GetMapping("/internal/evaluations/get-evaluations-by-student")
    List<Map<String, Object>> getEvaluationsByStudent(@RequestParam Integer studentId);
    
    @GetMapping("/internal/evaluations/get-score-statistics")
    Map<String, Object> getScoreStatistics(@RequestParam(required = false) String startDate, 
                                         @RequestParam(required = false) String endDate);
    
    @GetMapping("/internal/evaluations/get-pending-evaluations")
    Long getPendingEvaluations();
    
    @GetMapping("/internal/evaluations/get-pending-evaluations-by-evaluator")
    Long getPendingEvaluationsByEvaluator(@RequestParam Integer evaluatorId);
    
    @GetMapping("/internal/evaluations/get-pending-evaluations-list")
    List<Map<String, Object>> getPendingEvaluationsList();
}
