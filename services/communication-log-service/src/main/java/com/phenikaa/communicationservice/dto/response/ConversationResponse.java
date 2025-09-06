package com.phenikaa.communicationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationResponse {
    private String partnerId;
    private String partnerName;
    private String partnerEmail;
    private String partnerAvatar;
    private String lastMessage;
    private Instant lastMessageTime;
    private Long messageCount;
    private Long unreadCount;
    private String conversationId; // ID để identify conversation
}
