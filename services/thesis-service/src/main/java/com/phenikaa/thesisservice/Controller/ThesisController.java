package com.phenikaa.thesisservice.Controller;

import com.phenikaa.thesisservice.dto.request.CreateProjectTopicDTO;
import com.phenikaa.thesisservice.dto.request.EditProjectTopicDTO;
import com.phenikaa.thesisservice.dto.request.UpdateProjectTopicDTO;
import com.phenikaa.thesisservice.dto.response.ProjectTopicResponseDTO;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.service.interfaces.ThesisService;
import com.phenikaa.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/lecturer/thesis")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
public class ThesisController {

    private final ThesisService thesisService;
    private final JwtUtil jwtUtil;

    @PostMapping("/createTopic")
    public ProjectTopic createTopic(@RequestHeader("Authorization") String token,
                                    @RequestBody CreateProjectTopicDTO request) {
        Integer userId = jwtUtil.extractUserId(token);
        return thesisService.createProjectTopic(request, userId);
    }

    @GetMapping("/getListTopic")
    public List<ProjectTopicResponseDTO> getListTopic() {
        return thesisService.findAll();
    }

    @PostMapping("/editTopic")
    public ProjectTopic editTopic(@RequestBody EditProjectTopicDTO request) {
        return thesisService.editProjectTopic(request);
    }

    @PutMapping("/updateTopic")
    public ProjectTopic updateTopic(@RequestBody UpdateProjectTopicDTO request) {
        return thesisService.updateProjectTopic(request);
    }

    @DeleteMapping("/deleteTopic")
    public void deleteTopic(@RequestParam Integer topicId) {
        thesisService.deleteTopic(topicId);
    }

}

