package com.phenikaa.thesisservice.dto.response;

import com.phenikaa.thesisservice.entity.SuggestedTopic;
import lombok.Data;

import java.time.Instant;

@Data
public class GetSuggestTopicResponse {
    private Integer suggestedId;
    private Integer suggestedBy;
    private Integer suggestedFor;
    private SuggestedTopic.SuggestionStatus suggestionStatus;
    private Integer approvedBy;
    private Instant createdAt;
    private Integer topicId;
}
