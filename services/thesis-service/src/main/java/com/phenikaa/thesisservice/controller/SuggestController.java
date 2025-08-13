package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.dto.request.SuggestTopicRequest;
import com.phenikaa.utils.JwtUtil;
import com.phenikaa.thesisservice.service.interfaces.SuggestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/thesis-service/student")
@RequiredArgsConstructor
public class SuggestController {

    private final SuggestService suggestService;
    private final JwtUtil jwtUtil;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/suggest-topic")
    public ResponseEntity<String> suggestTopic(@RequestBody SuggestTopicRequest dto,
                                               @RequestHeader("Authorization") String token) {
        Integer studentId = jwtUtil.extractUserId(token);
        suggestService.suggestTopic(dto, studentId);
        return ResponseEntity.ok("Đã gửi đề xuất đề tài");
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/suggestions/{suggestedId}/accept")
    public ResponseEntity<String> acceptSuggestedTopic(@PathVariable Integer suggestedId,
                                                       @RequestHeader("Authorization") String token) {
        Integer userId = jwtUtil.extractUserId(token);
        suggestService.acceptSuggestedTopic(suggestedId, userId);
        return ResponseEntity.ok("Đề tài đã được chấp nhận");
    }
}
