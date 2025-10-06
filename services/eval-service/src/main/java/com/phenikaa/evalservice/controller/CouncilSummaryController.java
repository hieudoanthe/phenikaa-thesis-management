package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.entity.CouncilSummary;
import com.phenikaa.evalservice.service.CouncilSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/eval-service/teacher/council-summary")
@RequiredArgsConstructor
public class CouncilSummaryController {

    private final CouncilSummaryService service;

    @PostMapping("/{topicId}")
    public ResponseEntity<CouncilSummary> upsert(
            @PathVariable Integer topicId,
            @RequestBody Map<String, Object> body
    ) {
        Integer chairmanId = body.get("chairmanId") instanceof Number n ? ((Number) n).intValue() : null;
        String content = (String) body.getOrDefault("content", "");
        if (chairmanId == null || !service.hasChairmanAccess(chairmanId, topicId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(service.upsert(topicId, chairmanId, content));
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<CouncilSummary> get(@PathVariable Integer topicId) {
        return service.getByTopicId(topicId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{topicId}/access/{lecturerId}")
    public ResponseEntity<Map<String, Object>> hasAccess(@PathVariable Integer topicId, @PathVariable Integer lecturerId) {
        boolean has = service.hasChairmanAccess(lecturerId, topicId);
        return ResponseEntity.ok(java.util.Collections.singletonMap("hasAccess", has));
    }
}


