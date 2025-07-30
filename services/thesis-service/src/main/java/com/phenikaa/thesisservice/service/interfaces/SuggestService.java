package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.dto.request.SuggestTopicDTO;

public interface SuggestService {
    void suggestTopic(SuggestTopicDTO dto, Integer studentId);
    void acceptSuggestedTopic(Integer suggestedId, Integer approverId);
}
