package com.phenikaa.thesisservice.dto.request;

import com.phenikaa.thesisservice.entity.ProjectTopic;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Data
public class ThesisQbeFilterRequest {

    private String topicCode;
    private String title;
    private String description;
    private String objectives;
    private String methodology;

    private Integer supervisorId;
    private Integer academicYearId;

    private ProjectTopic.DifficultyLevel difficultyLevel;
    private ProjectTopic.TopicStatus topicStatus;
    private ProjectTopic.ApprovalStatus approvalStatus;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant createdFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant createdTo;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant updatedFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant updatedTo;

    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "topicId";
    private String sortDirection = "ASC";
}


