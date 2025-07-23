package com.phenikaa.thesisservice.dto.response;

import com.phenikaa.thesisservice.entity.ProjectTopic;
import lombok.Data;

@Data
public class AvailableTopicDTO {
    private Integer topicId;
    private String topicCode;
    private String title;
    private String description;
    private Integer maxStudents;
    private Integer currentStudents;
    private Integer supervisorId;
    private ProjectTopic.DifficultyLevel difficultyLevel;
}
