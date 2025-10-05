package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.service.interfaces.ThesisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/thesis")
@RequiredArgsConstructor
@Slf4j
public class InternalTopicController {
    
    private final ThesisService thesisService;
    
    @GetMapping("/topics/{topicId}")
    public ResponseEntity<Map<String, Object>> getTopicById(@PathVariable Integer topicId) {
        try {
            log.info("Getting topic by ID: {}", topicId);
            
            Map<String, Object> topicInfo = thesisService.getTopicById(topicId);
            return ResponseEntity.ok(topicInfo);
            
        } catch (Exception e) {
            log.error("Error getting topic by ID {}: {}", topicId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
