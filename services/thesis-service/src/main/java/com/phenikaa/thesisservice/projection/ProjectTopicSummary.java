package com.phenikaa.thesisservice.projection;

import com.phenikaa.thesisservice.entity.ProjectTopic;

// interface base
public interface ProjectTopicSummary {
    Integer getTopicId();
    String getTopicCode();
    String getTitle();
    ProjectTopic.DifficultyLevel getDifficultyLevel();
    ProjectTopic.ApprovalStatus getApprovalStatus();
}


