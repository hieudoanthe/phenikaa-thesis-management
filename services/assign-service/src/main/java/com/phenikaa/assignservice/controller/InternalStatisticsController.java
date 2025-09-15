package com.phenikaa.assignservice.controller;

import com.phenikaa.assignservice.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/assignments")
@RequiredArgsConstructor
@Slf4j
public class InternalStatisticsController {
    
    private final AssignmentService assignmentService;
    
    @GetMapping("/get-assignment-count")
    public Long getAssignmentCount() {
        log.info("Getting total assignment count");
        return assignmentService.getAssignmentCount();
    }
    
    @GetMapping("/get-assignment-count-by-status")
    public Long getAssignmentCountByStatus(@RequestParam String status) {
        log.info("Getting assignment count by status: {}", status);
        return assignmentService.getAssignmentCountByStatus(status);
    }
    
    @GetMapping("/get-assignment-count-by-user")
    public Long getAssignmentCountByUser(@RequestParam Integer userId) {
        log.info("Getting assignment count by user: {}", userId);
        return assignmentService.getAssignmentCountByUser(userId);
    }
    
    @GetMapping("/get-assignment-count-by-topic")
    public Long getAssignmentCountByTopic(@RequestParam Integer topicId) {
        log.info("Getting assignment count by topic: {}", topicId);
        return assignmentService.getAssignmentCountByTopic(topicId);
    }
    
    @GetMapping("/get-assignments-by-user")
    public List<Map<String, Object>> getAssignmentsByUser(@RequestParam Integer userId) {
        log.info("Getting assignments by user: {}", userId);
        return assignmentService.getAssignmentsByUser(userId);
    }
    
    @GetMapping("/get-assignments-by-topic")
    public List<Map<String, Object>> getAssignmentsByTopic(@RequestParam Integer topicId) {
        log.info("Getting assignments by topic: {}", topicId);
        return assignmentService.getAssignmentsByTopic(topicId);
    }
    
    @GetMapping("/get-task-count")
    public Long getTaskCount() {
        log.info("Getting total task count");
        return assignmentService.getTaskCount();
    }
    
    @GetMapping("/get-task-count-by-status")
    public Long getTaskCountByStatus(@RequestParam String status) {
        log.info("Getting task count by status: {}", status);
        return assignmentService.getTaskCountByStatus(status);
    }
    
    @GetMapping("/get-task-count-by-assignment")
    public Long getTaskCountByAssignment(@RequestParam Integer assignmentId) {
        log.info("Getting task count by assignment: {}", assignmentId);
        return assignmentService.getTaskCountByAssignment(assignmentId);
    }
    
    @GetMapping("/get-tasks-by-assignment")
    public List<Map<String, Object>> getTasksByAssignment(@RequestParam Integer assignmentId) {
        log.info("Getting tasks by assignment: {}", assignmentId);
        return assignmentService.getTasksByAssignment(assignmentId);
    }
}
