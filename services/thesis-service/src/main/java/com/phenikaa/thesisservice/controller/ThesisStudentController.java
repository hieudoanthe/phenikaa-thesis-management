package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.dto.request.RegisterTopicRequest;
import com.phenikaa.thesisservice.dto.request.SuggestTopicRequest;
import com.phenikaa.thesisservice.dto.request.ThesisSpecificationFilterRequest;
import com.phenikaa.thesisservice.dto.response.GetThesisResponse;
import com.phenikaa.thesisservice.dto.response.GetSuggestTopicResponse;
import com.phenikaa.thesisservice.service.interfaces.RegisterService;
import com.phenikaa.thesisservice.service.interfaces.ThesisService;
import com.phenikaa.utils.JwtUtil;
import com.phenikaa.thesisservice.service.interfaces.SuggestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/thesis-service/student")
@RequiredArgsConstructor
public class ThesisStudentController {

    private final RegisterService registerService;
    private final ThesisService thesisService;
    private final SuggestService suggestService;
    private final JwtUtil jwtUtil;

    @PostMapping("/suggest-topic")
    public ResponseEntity<String> suggestTopic(@RequestBody SuggestTopicRequest dto,
                                               @RequestHeader("Authorization") String token) {
        Integer studentId = jwtUtil.extractUserId(token);
        suggestService.suggestTopic(dto, studentId);
        return ResponseEntity.ok("Đã gửi đề xuất đề tài");
    }

    @PostMapping("/register-topic")
    public ResponseEntity<String> registerTopic(
            @RequestHeader("Authorization") String token,
            @RequestBody RegisterTopicRequest dto
    ) {
        Integer userId = jwtUtil.extractUserId(token);
        registerService.registerTopic(dto, userId);
        return ResponseEntity.ok("Registered successfully!");
    }

    @GetMapping("/available-topics")
    public ResponseEntity<Page<GetThesisResponse>> getAvailableTopics(
            @RequestHeader("Authorization") String token) {
        Integer userId = jwtUtil.extractUserId(token);
        
        // Tạo filter request để áp dụng logic mới
        ThesisSpecificationFilterRequest filterRequest = new ThesisSpecificationFilterRequest();
        filterRequest.setUserRole("STUDENT");
        filterRequest.setUserId(userId);
        filterRequest.setPage(0);
        filterRequest.setSize(1000); // Lấy tất cả đề tài
        filterRequest.setSortBy("topicId");
        filterRequest.setSortDirection("DESC");
        
        Page<GetThesisResponse> page = thesisService.filterTheses(filterRequest);
        return ResponseEntity.ok(page);
    }

    // Tìm kiếm/ lọc đề tài bằng Specification (dành cho sinh viên)
    @PostMapping("/topics/filter")
    public ResponseEntity<Page<GetThesisResponse>> filterTopicsForStudent(
            @RequestBody ThesisSpecificationFilterRequest request) {
        // Đảm bảo sinh viên chỉ thấy đề tài hợp lệ nếu FE không truyền
        if (request.getUserRole() == null) {
            request.setUserRole("STUDENT");
        }
        Page<GetThesisResponse> page = thesisService.filterTheses(request);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/get-suggest-topic-{studentId}/paged")
    public ResponseEntity<Page<GetSuggestTopicResponse>> getSuggestTopicByStudentId(
            @PathVariable Integer studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {

        Page<GetSuggestTopicResponse> suggestTopic = suggestService.getSuggestTopicByStudentId(studentId, page, size);

        return ResponseEntity.ok(suggestTopic);
    }

    @PutMapping("/update-suggest-topic/{suggestedId}")
    public ResponseEntity<String> updateSuggestTopic(
            @PathVariable Integer suggestedId,
            @RequestBody SuggestTopicRequest dto,
            @RequestHeader("Authorization") String token) {
        Integer studentId = jwtUtil.extractUserId(token);
        suggestService.updateSuggestTopic(suggestedId, dto, studentId);
        return ResponseEntity.ok("Đã cập nhật đề xuất đề tài");
    }

}
