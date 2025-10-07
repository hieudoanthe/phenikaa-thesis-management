package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.entity.SupervisorSummary;
import com.phenikaa.evalservice.service.SupervisorSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/eval-service/teacher/supervisor-summary")
@RequiredArgsConstructor
public class SupervisorSummaryController {
    private final SupervisorSummaryService service;

    @GetMapping("/{topicId}")
    public ResponseEntity<SupervisorSummary> get(@PathVariable Integer topicId) {
        return service.getByTopicId(topicId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{topicId}")
    public ResponseEntity<SupervisorSummary> upsert(@PathVariable Integer topicId, @RequestBody Map<String, Object> body) {
        Integer supervisorId = body.get("supervisorId") instanceof Number n ? ((Number) n).intValue() : null;
        String content = (String) body.getOrDefault("content", "");
        if (supervisorId == null || !service.hasSupervisorAccess(supervisorId, topicId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(service.upsert(topicId, supervisorId, content));
    }

    @GetMapping("/{topicId}/access/{lecturerId}")
    public ResponseEntity<Map<String, Object>> hasAccess(@PathVariable Integer topicId, @PathVariable Integer lecturerId) {
        boolean has = service.hasSupervisorAccess(lecturerId, topicId);
        return ResponseEntity.ok(java.util.Collections.singletonMap("hasAccess", has));
    }
}


