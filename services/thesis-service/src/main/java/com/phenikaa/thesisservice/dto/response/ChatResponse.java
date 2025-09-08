package com.phenikaa.thesisservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String message;
    private String sessionId;
    private List<TopicSuggestion> topicSuggestions;
    private List<LecturerSuggestion> lecturerSuggestions;
    private String responseType; // "general", "topic_suggestion", "lecturer_suggestion", "capacity_check"
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicSuggestion {
        private String title;
        private String description;
        private String objectives;
        private String methodology;
        private String difficultyLevel;
        private String expectedOutcome;
        private String technologies;
        private String reason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LecturerSuggestion {
        private Integer lecturerId;
        private String lecturerName;
        private String specialization;
        private Integer remainingCapacity;
        private String phone;
    }
}
