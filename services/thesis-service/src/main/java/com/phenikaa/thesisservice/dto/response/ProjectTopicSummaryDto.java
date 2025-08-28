package com.phenikaa.thesisservice.dto.response;

import com.phenikaa.thesisservice.entity.ProjectTopic;

// DTO Projection (class-based): dùng với JPQL constructor expression
public record ProjectTopicSummaryDto(Integer topicId, String topicCode, String title,
                                     ProjectTopic.DifficultyLevel difficultyLevel,
                                     ProjectTopic.ApprovalStatus approvalStatus) {

}


