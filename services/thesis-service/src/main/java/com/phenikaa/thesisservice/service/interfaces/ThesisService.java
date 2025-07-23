package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.dto.request.CreateProjectTopicDTO;
import com.phenikaa.thesisservice.dto.request.EditProjectTopicDTO;
import com.phenikaa.thesisservice.dto.request.RegisterTopicDTO;
import com.phenikaa.thesisservice.dto.request.UpdateProjectTopicDTO;
import com.phenikaa.thesisservice.dto.response.AvailableTopicDTO;
import com.phenikaa.thesisservice.dto.response.ProjectTopicResponseDTO;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.entity.Register;

import java.util.List;

public interface ThesisService {
    ProjectTopic createProjectTopic(CreateProjectTopicDTO dto, Integer userId);
    List<ProjectTopicResponseDTO> findAll();
    ProjectTopic editProjectTopic(EditProjectTopicDTO dto);
    ProjectTopic updateProjectTopic(UpdateProjectTopicDTO dto);
    void deleteTopic(Integer topicId);
    List<AvailableTopicDTO> getAvailableTopics();
}
