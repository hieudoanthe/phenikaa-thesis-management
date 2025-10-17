package com.phenikaa.thesisservice.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface SuggestionService {
    Integer saveSuggestionHistory(Integer studentId, String requestText, String specialization, String suggestionsJson);
    Page<Map<String, Object>> getSuggestionHistory(Integer studentId, Pageable pageable);
    void rateSuggestedTopic(Integer studentId, String topicTitle, String feedback); // LIKE/NEUTRAL/DISLIKE
    List<Map<String, Object>> findSimilarTopics(String topicTitle, Integer limit);
    void upsertPreference(Integer studentId, String areas, String keywords, String types);
}


