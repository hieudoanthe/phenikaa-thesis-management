package com.phenikaa.thesisservice.mapper;

import com.phenikaa.thesisservice.dto.request.EditProjectTopicDTO;
import com.phenikaa.thesisservice.dto.request.UpdateProjectTopicDTO;
import com.phenikaa.thesisservice.dto.response.AvailableTopicDTO;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProjectTopicMapper {

    void editProjectTopic(EditProjectTopicDTO dto, @MappingTarget ProjectTopic entity);
    void updateProjectTopic(UpdateProjectTopicDTO dto, @MappingTarget ProjectTopic entity);
    @Mapping(target = "currentStudents", expression = "java(projectTopic.getRegisters() != null ? (int) projectTopic.getRegisters().stream().filter(r -> r.getRegisterStatus() == com.phenikaa.thesisservice.entity.Register.RegisterStatus.APPROVED).count() : 0)")
    AvailableTopicDTO toAvailableTopicDTO(ProjectTopic projectTopic);
}

