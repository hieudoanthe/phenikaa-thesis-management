package com.phenikaa.thesisservice.projection;

import com.phenikaa.thesisservice.entity.ProjectTopic;

/**
 * Projection giao diện: chỉ lấy một số trường cần thiết của ProjectTopic
 */
public interface ProjectTopicSummary {
    Integer getTopicId();
    String getTopicCode();
    String getTitle();
    ProjectTopic.DifficultyLevel getDifficultyLevel();
    ProjectTopic.ApprovalStatus getApprovalStatus();
}


