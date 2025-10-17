package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.service.interfaces.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/thesis-service/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;

    @GetMapping("/history")
    public ResponseEntity<Page<Map<String, Object>>> history(
            @RequestParam Integer studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(suggestionService.getSuggestionHistory(studentId, PageRequest.of(page, size)));
    }

    @PostMapping("/rate")
    public ResponseEntity<Void> rate(
            @RequestParam Integer studentId,
            @RequestParam String topicTitle,
            @RequestParam String feedback // LIKE/NEUTRAL/DISLIKE
    ) {
        suggestionService.rateSuggestedTopic(studentId, topicTitle, feedback);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/similar")
    public ResponseEntity<List<Map<String, Object>>> similar(
            @RequestParam String topicTitle,
            @RequestParam(defaultValue = "5") Integer limit
    ) {
        return ResponseEntity.ok(suggestionService.findSimilarTopics(topicTitle, limit));
    }

    @PostMapping("/preferences")
    public ResponseEntity<Void> upsertPreference(
            @RequestParam Integer studentId,
            @RequestParam(required = false) String areas,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String types
    ) {
        suggestionService.upsertPreference(studentId, areas, keywords, types);
        return ResponseEntity.ok().build();
    }
}


