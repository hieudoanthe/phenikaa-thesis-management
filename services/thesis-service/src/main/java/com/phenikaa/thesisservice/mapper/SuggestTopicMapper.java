package com.phenikaa.thesisservice.mapper;

import com.phenikaa.thesisservice.dto.request.SuggestTopicDTO;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.entity.SuggestedTopic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface SuggestTopicMapper {

    @Mapping(target = "topicId", ignore = true)
    @Mapping(target = "topicCode", ignore = true)
    @Mapping(target = "academicYearId", ignore = true)
    @Mapping(target = "maxStudents", ignore = true)
    @Mapping(target = "difficultyLevel", ignore = true)
    @Mapping(target = "suggestedTopics", ignore = true)
    @Mapping(target = "registers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "topicStatus", constant = "INACTIVE")
    @Mapping(target = "approvalStatus", constant = "PENDING")
    ProjectTopic toProjectTopic(SuggestTopicDTO dto);

    // Tạo SuggestedTopic từ thông tin rời
    default SuggestedTopic toSuggestedTopic(Integer topicId, Integer suggestedBy, Integer suggestedFor, String reason) {
        return SuggestedTopic.builder()
                .topicId(topicId)
                .suggestedBy(suggestedBy)
                .suggestedFor(suggestedFor)
                .reason(reason)
                .suggestionStatus(SuggestedTopic.SuggestionStatus.PENDING)
                .build();
    }
}
