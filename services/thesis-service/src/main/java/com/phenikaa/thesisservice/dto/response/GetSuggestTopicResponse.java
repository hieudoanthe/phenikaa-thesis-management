package com.phenikaa.thesisservice.dto.response;

import com.phenikaa.thesisservice.entity.SuggestedTopic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetSuggestTopicResponse {
    private Integer suggestedId;
    private Integer suggestedBy;
    private Integer suggestedFor;
    private SuggestedTopic.SuggestionStatus suggestionStatus;
    private Integer approvedBy;
    private LocalDateTime createdAt;
    private Integer topicId;
}
