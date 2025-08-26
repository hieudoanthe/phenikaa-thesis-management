package com.phenikaa.thesisservice.mapper;

import com.phenikaa.thesisservice.dto.request.SuggestTopicRequest;
import com.phenikaa.thesisservice.dto.response.GetSuggestTopicResponse;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.entity.SuggestedTopic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface SuggestTopicMapper {

    @Mapping(target = "topicId", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "topicStatus", constant = "INACTIVE")
    @Mapping(target = "approvalStatus", constant = "PENDING")
    @Mapping(target = "createdBy", ignore = true)
    ProjectTopic toProjectTopic(SuggestTopicRequest dto);

    @Mapping(target = "topicId", source = "projectTopic.topicId")
    GetSuggestTopicResponse toGetSuggestTopicResponse(SuggestedTopic dto);
}
