package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.dto.request.CreateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.EditProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.UpdateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.response.AvailableTopicResponse;
import com.phenikaa.thesisservice.dto.response.GetThesisResponse;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ThesisService {
    ProjectTopic createProjectTopic(CreateProjectTopicRequest dto, Integer userId);
    List<GetThesisResponse> findAll();
    Page<GetThesisResponse> getTopicsByTeacherId(Integer teacherId, int page, int size);
    ProjectTopic editProjectTopic(EditProjectTopicRequest dto);
    ProjectTopic updateProjectTopic(UpdateProjectTopicRequest dto);
    void deleteTopic(Integer topicId);
    List<AvailableTopicResponse> getAvailableTopics();
    void approvedTopic(Integer topicId);
    void rejectTopic(Integer topicId);
}
