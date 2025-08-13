package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.dto.response.AvailableTopicResponse;
import com.phenikaa.utils.JwtUtil;
import com.phenikaa.thesisservice.dto.request.RegisterTopicRequest;
import com.phenikaa.thesisservice.service.interfaces.RegisterService;
import com.phenikaa.thesisservice.service.interfaces.TopicProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/thesis-service/student")
@RequiredArgsConstructor
public class RegisterController {
    private final RegisterService registerService;
    private final TopicProjectService thesisService;
    private final JwtUtil jwtUtil;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/register-topic")
    public ResponseEntity<String> registerTopic(
            @RequestHeader("Authorization") String token,
            @RequestBody RegisterTopicRequest dto
    ) {
        Integer userId = jwtUtil.extractUserId(token);
        registerService.registerTopic(dto, userId);
        return ResponseEntity.ok("Registered successfully!");
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/available-topics")
    public List<AvailableTopicResponse> getAvailableTopics() {
        return thesisService.getAvailableTopics();
    }

}
