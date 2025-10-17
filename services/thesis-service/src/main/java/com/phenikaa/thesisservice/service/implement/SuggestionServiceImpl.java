package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.entity.SuggestionHistory;
import com.phenikaa.thesisservice.entity.SuggestedTopicFeedback;
import com.phenikaa.thesisservice.entity.StudentPreference;
import com.phenikaa.thesisservice.repository.SuggestionHistoryRepository;
import com.phenikaa.thesisservice.repository.SuggestedTopicFeedbackRepository;
import com.phenikaa.thesisservice.repository.StudentPreferenceRepository;
import com.phenikaa.thesisservice.service.interfaces.SuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestionServiceImpl implements SuggestionService {

    private final SuggestionHistoryRepository historyRepository;
    private final SuggestedTopicFeedbackRepository feedbackRepository;
    private final StudentPreferenceRepository preferenceRepository;

    @Override
    public Integer saveSuggestionHistory(Integer studentId, String requestText, String specialization, String suggestionsJson) {
        SuggestionHistory s = SuggestionHistory.builder()
                .studentId(studentId)
                .requestText(requestText)
                .specialization(specialization)
                .suggestionsJson(suggestionsJson)
                .build();
        historyRepository.save(s);
        return s.getId();
    }

    @Override
    public Page<Map<String, Object>> getSuggestionHistory(Integer studentId, Pageable pageable) {
        return historyRepository.findByStudentIdOrderByCreatedAtDesc(studentId, pageable)
                .map(h -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", h.getId());
                    m.put("requestText", h.getRequestText());
                    m.put("specialization", h.getSpecialization());
                    m.put("suggestionsJson", h.getSuggestionsJson());
                    m.put("createdAt", h.getCreatedAt());
                    return m;
                });
    }

    @Override
    public void rateSuggestedTopic(Integer studentId, String topicTitle, String feedback) {
        SuggestedTopicFeedback.FeedbackType type;
        try {
            type = SuggestedTopicFeedback.FeedbackType.valueOf(feedback.toUpperCase());
        } catch (Exception e) {
            type = SuggestedTopicFeedback.FeedbackType.NEUTRAL;
        }
        SuggestedTopicFeedback fb = SuggestedTopicFeedback.builder()
                .studentId(studentId)
                .topicTitle(topicTitle)
                .feedback(type)
                .build();
        feedbackRepository.save(fb);
    }

    @Override
    public List<Map<String, Object>> findSimilarTopics(String topicTitle, Integer limit) {
        // Placeholder: in real app, implement vector/semantic search.
        // Return empty for now.
        return List.of();
    }

    @Override
    public void upsertPreference(Integer studentId, String areas, String keywords, String types) {
        StudentPreference pref = preferenceRepository.findByStudentId(studentId)
                .orElseGet(() -> StudentPreference.builder().studentId(studentId).build());
        if (areas != null) pref.setAreas(areas);
        if (keywords != null) pref.setKeywords(keywords);
        if (types != null) pref.setTypes(types);
        preferenceRepository.save(pref);
    }
}


