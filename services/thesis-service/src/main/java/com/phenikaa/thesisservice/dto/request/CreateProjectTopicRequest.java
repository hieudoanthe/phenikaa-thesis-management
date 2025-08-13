package com.phenikaa.thesisservice.dto.request;

import com.phenikaa.thesisservice.entity.ProjectTopic;
import lombok.Data;

@Data
public class CreateProjectTopicRequest {
    private String topicCode;
    private String title;
    private String description;
    private String objectives;
    private String methodology;
    private String expectedOutcome;
    private Integer academicYearId;
    private Integer maxStudents;
    private ProjectTopic.DifficultyLevel difficultyLevel;
}
