package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.dto.request.EditProjectTopicRequest;
import com.phenikaa.thesisservice.dto.response.GetThesisResponse;
import com.phenikaa.thesisservice.service.interfaces.ThesisService;
import com.phenikaa.utils.JwtUtil;
import com.phenikaa.thesisservice.dto.request.CreateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.UpdateProjectTopicRequest;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/thesis-service/teacher")
@RequiredArgsConstructor
public class ThesisAdminController {

    private final ThesisService thesisService;
    private final JwtUtil jwtUtil;

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/create-topic")
    public ProjectTopic createTopic(@RequestHeader("Authorization") String token,
                                    @RequestBody CreateProjectTopicRequest request) {
        Integer userId = jwtUtil.extractUserId(token);
        return thesisService.createProjectTopic(request, userId);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/get-list-topic")
    public List<GetThesisResponse> getListTopic() {
        return thesisService.findAll();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/get-topic-by-{teacherId}/paged")
    public ResponseEntity<Page<GetThesisResponse>> getTopicsByTeacherId(
            @PathVariable Integer teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        Page<GetThesisResponse> topics =
                thesisService.getTopicsByTeacherId(teacherId, page, size);

        return ResponseEntity.ok(topics);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/edit-topic")
    public ProjectTopic editTopic(@RequestBody EditProjectTopicRequest request) {
        return thesisService.editProjectTopic(request);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/update-topic")
    public ProjectTopic updateTopic(@RequestBody UpdateProjectTopicRequest request) {
        return thesisService.updateProjectTopic(request);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/delete-topic")
    public void deleteTopic(@RequestParam Integer topicId) {
        thesisService.deleteTopic(topicId);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PatchMapping("/approve-topic")
    public ResponseEntity<Void> approveTopic(@RequestParam Integer topicId) {
        thesisService.approvedTopic(topicId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PatchMapping("/reject-topic")
    public ResponseEntity<Void> rejectTopic(@RequestParam Integer topicId) {
        thesisService.rejectTopic(topicId);
        return ResponseEntity.noContent().build();
    }

}

