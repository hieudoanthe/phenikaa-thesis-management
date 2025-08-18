package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.dto.request.EditProjectTopicRequest;
import com.phenikaa.utils.JwtUtil;
import com.phenikaa.thesisservice.dto.request.CreateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.UpdateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.response.ProjectTopicResponse;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.service.interfaces.TopicProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/thesis-service/teacher")
@RequiredArgsConstructor
public class TopicProjectController {

    private final TopicProjectService topicProjectService;
    private final JwtUtil jwtUtil;

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/create-topic")
    public ProjectTopic createTopic(@RequestHeader("Authorization") String token,
                                    @RequestBody CreateProjectTopicRequest request) {
        Integer userId = jwtUtil.extractUserId(token);
        return topicProjectService.createProjectTopic(request, userId);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/get-list-topic")
    public List<ProjectTopicResponse> getListTopic() {
        return topicProjectService.findAll();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/get-topic-by-/{teacherId}")
    public List<ProjectTopicResponse> getTopicByTeacherId(@PathVariable Integer teacherId) {
        return topicProjectService.getTopicsByTeacherId(teacherId);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/get-topic-by-{teacherId}/paged")
    public ResponseEntity<Page<ProjectTopicResponse>> getTopicsByTeacherId(
            @PathVariable Integer teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        Page<ProjectTopicResponse> topics =
                topicProjectService.getTopicsByTeacherId(teacherId, page, size);

        return ResponseEntity.ok(topics);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/edit-topic")
    public ProjectTopic editTopic(@RequestBody EditProjectTopicRequest request) {
        return topicProjectService.editProjectTopic(request);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/update-topic")
    public ProjectTopic updateTopic(@RequestBody UpdateProjectTopicRequest request) {
        return topicProjectService.updateProjectTopic(request);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/delete-topic")
    public void deleteTopic(@RequestParam Integer topicId) {
        topicProjectService.deleteTopic(topicId);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PatchMapping("/approve-topic")
    public ResponseEntity<Void> approveTopic(@RequestParam Integer topicId) {
        topicProjectService.approvedTopic(topicId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PatchMapping("/reject-topic")
    public ResponseEntity<Void> rejectTopic(@RequestParam Integer topicId) {
        topicProjectService.rejectTopic(topicId);
        return ResponseEntity.noContent().build();
    }

}

