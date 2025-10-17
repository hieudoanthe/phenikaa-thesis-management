package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.dto.request.ChatRequest;
import com.phenikaa.thesisservice.dto.response.ChatResponse;

public interface AiChatService {
    ChatResponse processChatMessage(ChatRequest request);
    ChatResponse suggestTopics(String userMessage, String specialization);
    ChatResponse suggestTopics(String userMessage, String specialization, Integer studentId);
    ChatResponse findSuitableLecturers(String userMessage, String specialization);
    ChatResponse checkLecturerCapacity(Integer lecturerId);
    ChatResponse getGeneralHelp();
}
