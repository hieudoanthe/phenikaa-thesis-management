package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.dto.request.RegisterTopicDTO;
import com.phenikaa.thesisservice.dto.response.AvailableTopicDTO;
import com.phenikaa.thesisservice.entity.Register;
import com.phenikaa.thesisservice.service.interfaces.RegisterService;
import com.phenikaa.thesisservice.service.interfaces.ThesisService;
import com.phenikaa.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer/thesis")
@RequiredArgsConstructor
public class RegisterController {
    private final RegisterService registerService;
    private final ThesisService thesisService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register-topic")
    public ResponseEntity<Register> registerTopic(
            @RequestHeader("Authorization") String token,
            @RequestBody RegisterTopicDTO dto
    ) {
        Integer userId = jwtUtil.extractUserId(token);
        Register register = registerService.registerTopic(dto, userId);
        return ResponseEntity.ok(register);
    }

    @GetMapping("/available-topics")
    public List<AvailableTopicDTO> getAvailableTopics() {
        return thesisService.getAvailableTopics();
    }

}
