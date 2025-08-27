package com.phenikaa.thesisservice.dto.response;

import com.phenikaa.thesisservice.entity.ProjectTopic;

/**
 * DTO Projection (class-based): dùng với JPQL constructor expression
 */
public class ProjectTopicSummaryDto {
    private final Integer topicId;
    private final String topicCode;
    private final String title;
    private final ProjectTopic.DifficultyLevel difficultyLevel;
    private final ProjectTopic.ApprovalStatus approvalStatus;

    public ProjectTopicSummaryDto(Integer topicId, String topicCode, String title,
                                  ProjectTopic.DifficultyLevel difficultyLevel,
                                  ProjectTopic.ApprovalStatus approvalStatus) {
        this.topicId = topicId;
        this.topicCode = topicCode;
        this.title = title;
        this.difficultyLevel = difficultyLevel;
        this.approvalStatus = approvalStatus;
    }

    public Integer getTopicId() { return topicId; }
    public String getTopicCode() { return topicCode; }
    public String getTitle() { return title; }
    public ProjectTopic.DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public ProjectTopic.ApprovalStatus getApprovalStatus() { return approvalStatus; }
}


