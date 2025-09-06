package com.phenikaa.communicationservice.service.interfaces;

import com.phenikaa.communicationservice.dto.response.ConversationResponse;
import reactor.core.publisher.Flux;

public interface ConversationService {
    Flux<ConversationResponse> getUserConversations(String userId);
    Flux<ConversationResponse> getUserConversationsWithDetails(String userId);
}
