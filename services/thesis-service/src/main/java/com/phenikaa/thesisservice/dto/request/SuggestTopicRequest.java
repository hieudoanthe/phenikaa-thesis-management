package com.phenikaa.thesisservice.dto.request;

import lombok.Data;

@Data
public class SuggestTopicRequest {
    private String topicCode;
    private String title;
    private String description;
    private String objectives;
    private String methodology;
    private String expectedOutcome;
    private Integer supervisorId;
    private String reason;
}
