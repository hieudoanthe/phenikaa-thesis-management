package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.dto.request.CreateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.EditProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.UpdateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.response.AvailableTopicResponse;
import com.phenikaa.thesisservice.dto.response.ProjectTopicResponse;
import com.phenikaa.thesisservice.mapper.ProjectTopicMapper;
import com.phenikaa.thesisservice.repository.ProjectTopicRepository;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.service.interfaces.TopicProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicProjectServiceImpl implements TopicProjectService {

    private final ProjectTopicRepository projectTopicRepository;

    private final ProjectTopicMapper projectTopicMapper;

    @Override
    public ProjectTopic createProjectTopic(CreateProjectTopicRequest dto, Integer userId) {
        ProjectTopic entity = ProjectTopic.builder()
                .topicCode(dto.getTopicCode())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .objectives(dto.getObjectives())
                .methodology(dto.getMethodology())
                .expectedOutcome(dto.getExpectedOutcome())
                .maxStudents(dto.getMaxStudents())
                .academicYearId(dto.getAcademicYearId())
                .difficultyLevel(dto.getDifficultyLevel())
                .createdBy(userId)
                .supervisorId(userId)
                .updatedBy(userId)
                .build();
        return projectTopicRepository.save(entity);
    }

    @Override
    public List<ProjectTopicResponse> findAll() {
        return projectTopicRepository.findAll()
                .stream()
                .map(e -> ProjectTopicResponse.builder()
                        .topicId(e.getTopicId())
                        .topicCode(e.getTopicCode())
                        .title(e.getTitle())
                        .description(e.getDescription())
                        .objectives(e.getObjectives())
                        .methodology(e.getMethodology())
                        .expectedOutcome(e.getExpectedOutcome())
                        .academicYearId(e.getAcademicYearId())
                        .maxStudents(e.getMaxStudents())
                        .difficultyLevel(e.getDifficultyLevel())
                        .topicStatus(e.getTopicStatus())
                        .approvalStatus(e.getApprovalStatus())
                        .registerId(
                                e.getRegisters() != null && !e.getRegisters().isEmpty()
                                        ? e.getRegisters().getFirst().getRegisterId()
                                        : null
                        )
                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    public ProjectTopic editProjectTopic(EditProjectTopicRequest dto) {
        ProjectTopic entity = projectTopicRepository.findById(dto.getTopicId()).orElseThrow(() -> new RuntimeException("Not found"));
        projectTopicMapper.editProjectTopic(dto, entity);
        return projectTopicRepository.save(entity);
    }

    @Override
    public ProjectTopic updateProjectTopic(UpdateProjectTopicRequest dto) {
        ProjectTopic entity = projectTopicRepository.findById(dto.getTopicId()).orElseThrow(() -> new RuntimeException("Not found"));
        projectTopicMapper.updateProjectTopic(dto, entity);
        return projectTopicRepository.save(entity);
    }

    @Override
    public void deleteTopic(Integer topicId) {
        ProjectTopic entity = projectTopicRepository.findById(topicId).orElseThrow(() -> new RuntimeException("Not found"));
        projectTopicRepository.delete(entity);
    }

    @Override
    public List<AvailableTopicResponse> getAvailableTopics() {
        List<ProjectTopic> topics = projectTopicRepository.findByApprovalStatusAndTopicStatus(
                ProjectTopic.ApprovalStatus.AVAILABLE,
                ProjectTopic.TopicStatus.ACTIVE
        );

        return topics.stream()
                .map(projectTopicMapper::toAvailableTopicDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void approvedTopic(Integer topicId) {
        Optional<ProjectTopic> projectTopicOpt = projectTopicRepository.findById(topicId);
        if (projectTopicOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project topic not found!");
        }
        ProjectTopic projectTopic = projectTopicOpt.get();
        projectTopic.setApprovalStatus(ProjectTopic.ApprovalStatus.APPROVED);
        projectTopicRepository.save(projectTopic);
    }

    @Override
    public void rejectTopic(Integer topicId) {
        Optional<ProjectTopic> projectTopicOpt = projectTopicRepository.findById(topicId);
        if (projectTopicOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project topic not found!");
        }
        ProjectTopic projectTopic = projectTopicOpt.get();
        projectTopic.setApprovalStatus(ProjectTopic.ApprovalStatus.REJECTED);
        projectTopicRepository.save(projectTopic);
    }

}
