package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.dto.request.SuggestTopicRequest;

public interface SuggestService {
    void suggestTopic(SuggestTopicRequest dto, Integer studentId);
    void acceptSuggestedTopic(Integer suggestedId, Integer approverId);
}
