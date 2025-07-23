package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.client.ProfileServiceClient;
import com.phenikaa.thesisservice.dto.request.CreateProjectTopicDTO;
import com.phenikaa.thesisservice.dto.request.EditProjectTopicDTO;
import com.phenikaa.thesisservice.dto.request.RegisterTopicDTO;
import com.phenikaa.thesisservice.dto.request.UpdateProjectTopicDTO;
import com.phenikaa.thesisservice.dto.response.AvailableTopicDTO;
import com.phenikaa.thesisservice.dto.response.ProjectTopicResponseDTO;
import com.phenikaa.thesisservice.entity.Register;
import com.phenikaa.thesisservice.mapper.ProjectTopicMapper;
import com.phenikaa.thesisservice.mapper.RegisterMapper;
import com.phenikaa.thesisservice.repository.ProjectTopicRepository;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.repository.RegisterRepository;
import com.phenikaa.thesisservice.service.interfaces.ThesisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThesisServiceImpl implements ThesisService {

    private final ProjectTopicRepository projectTopicRepository;

    private final ProjectTopicMapper projectTopicMapper;

    private final ProfileServiceClient profileServiceClient;

    @Override
    public ProjectTopic createProjectTopic(CreateProjectTopicDTO dto, Integer userId) {
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
    public List<ProjectTopicResponseDTO> findAll() {
        return projectTopicRepository.findAll()
                .stream()
                .map(e -> ProjectTopicResponseDTO.builder()
                        .topicId(e.getTopicId())
                        .topicCode(e.getTopicCode())
                        .title(e.getTitle())
                        .description(e.getDescription())
                        .objectives(e.getObjectives())
                        .methodology(e.getMethodology())
                        .expectedOutcome(e.getExpectedOutcome())
                        .supervisorId(e.getSupervisorId())
                        .academicYearId(e.getAcademicYearId())
                        .maxStudents(e.getMaxStudents())
                        .difficultyLevel(e.getDifficultyLevel())
                        .topicStatus(e.getTopicStatus())
                        .createdAt(e.getCreatedAt())
                        .updatedAt(e.getUpdatedAt())
                        .approvalStatus(e.getApprovalStatus())
                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    public ProjectTopic editProjectTopic(EditProjectTopicDTO dto) {
        ProjectTopic entity = projectTopicRepository.findById(dto.getTopicId()).orElseThrow(() -> new RuntimeException("Not found"));
        projectTopicMapper.editProjectTopic(dto, entity);
        return projectTopicRepository.save(entity);
    }

    @Override
    public ProjectTopic updateProjectTopic(UpdateProjectTopicDTO dto) {
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
    public List<AvailableTopicDTO> getAvailableTopics() {
        List<ProjectTopic> topics = projectTopicRepository.findByApprovalStatusAndTopicStatus(
                ProjectTopic.ApprovalStatus.AVAILABLE,
                ProjectTopic.TopicStatus.ACTIVE
        );

        return topics.stream()
                .map(projectTopicMapper::toAvailableTopicDTO)
                .collect(Collectors.toList());
    }

}
