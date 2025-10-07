package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.entity.ReviewerSummary;
import com.phenikaa.evalservice.service.ReviewerSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/eval-service/teacher/reviewer-summary")
@RequiredArgsConstructor
public class ReviewerSummaryController {
    private final ReviewerSummaryService service;

    @GetMapping("/{topicId}")
    public ResponseEntity<ReviewerSummary> get(@PathVariable Integer topicId) {
        return service.getByTopicId(topicId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{topicId}")
    public ResponseEntity<ReviewerSummary> upsert(@PathVariable Integer topicId, @RequestBody Map<String, Object> body) {
        Integer reviewerId = body.get("reviewerId") instanceof Number n ? ((Number) n).intValue() : null;
        String content = (String) body.getOrDefault("content", "");
        if (reviewerId == null || !service.hasReviewerAccess(reviewerId, topicId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(service.upsert(topicId, reviewerId, content));
    }

    @GetMapping("/{topicId}/access/{lecturerId}")
    public ResponseEntity<Map<String, Object>> hasAccess(@PathVariable Integer topicId, @PathVariable Integer lecturerId) {
        boolean has = service.hasReviewerAccess(lecturerId, topicId);
        return ResponseEntity.ok(java.util.Collections.singletonMap("hasAccess", has));
    }
}
