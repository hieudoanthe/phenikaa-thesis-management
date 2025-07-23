package com.phenikaa.thesisservice.dto.response;

import com.phenikaa.thesisservice.entity.ProjectTopic;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectTopicResponseDTO {
    private Integer topicId;
    private String topicCode;
    private String title;
    private String description;
    private String objectives;
    private String methodology;
    private String expectedOutcome;
    private Integer supervisorId;
    private Integer academicYearId;
    private Integer maxStudents;
    private ProjectTopic.DifficultyLevel difficultyLevel;
    private ProjectTopic.TopicStatus topicStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProjectTopic.ApprovalStatus approvalStatus;

}
