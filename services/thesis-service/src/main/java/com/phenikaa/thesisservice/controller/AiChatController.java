package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.dto.request.ChatRequest;
import com.phenikaa.thesisservice.dto.response.ChatResponse;
import com.phenikaa.thesisservice.service.interfaces.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/thesis-service/ai-chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            log.info("Received chat request: {}", request.getMessage());
            ChatResponse response = aiChatService.processChatMessage(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.builder()
                            .message("Xin lỗi, có lỗi xảy ra khi xử lý yêu cầu của bạn.")
                            .sessionId(request.getSessionId())
                            .responseType("error")
                            .build());
        }
    }

    @PostMapping("/suggest-topics")
    public ResponseEntity<ChatResponse> suggestTopics(
            @RequestParam String message,
            @RequestParam(required = false, defaultValue = "Công nghệ thông tin") String specialization) {
        try {
            log.info("Received topic suggestion request: {}", message);
            ChatResponse response = aiChatService.suggestTopics(message, specialization);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error suggesting topics", e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.builder()
                            .message("Xin lỗi, không thể gợi ý đề tài lúc này.")
                            .responseType("error")
                            .build());
        }
    }

    @PostMapping("/find-lecturers")
    public ResponseEntity<ChatResponse> findLecturers(
            @RequestParam String message,
            @RequestParam(required = false, defaultValue = "Công nghệ thông tin") String specialization) {
        try {
            log.info("Received lecturer search request: {}", message);
            ChatResponse response = aiChatService.findSuitableLecturers(message, specialization);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error finding lecturers", e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.builder()
                            .message("Xin lỗi, không thể tìm giảng viên lúc này.")
                            .responseType("error")
                            .build());
        }
    }

    @GetMapping("/check-capacity/{lecturerId}")
    public ResponseEntity<ChatResponse> checkLecturerCapacity(@PathVariable Integer lecturerId) {
        try {
            log.info("Checking capacity for lecturer: {}", lecturerId);
            ChatResponse response = aiChatService.checkLecturerCapacity(lecturerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking lecturer capacity", e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.builder()
                            .message("Xin lỗi, không thể kiểm tra capacity của giảng viên.")
                            .responseType("error")
                            .build());
        }
    }

    @GetMapping("/help")
    public ResponseEntity<ChatResponse> getHelp() {
        try {
            ChatResponse response = aiChatService.getGeneralHelp();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting help", e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.builder()
                            .message("Xin lỗi, không thể tải thông tin trợ giúp.")
                            .responseType("error")
                            .build());
        }
    }
}
