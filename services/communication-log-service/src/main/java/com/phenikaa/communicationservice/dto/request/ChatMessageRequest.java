package com.phenikaa.communicationservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageRequest {
    private String senderId;
    private String receiverId;
    private String content;
    private Instant timestamp;
}
