package com.phenikaa.communicationservice.controller;

import com.phenikaa.communicationservice.dto.request.ChatMessageRequest;
import com.phenikaa.communicationservice.dto.response.ConversationResponse;
import com.phenikaa.communicationservice.entity.ChatMessage;
import com.phenikaa.communicationservice.service.interfaces.ChatService;
import com.phenikaa.communicationservice.service.interfaces.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ConversationService conversationService;

    @PostMapping("/send")
    public Mono<ChatMessage> sendMessage(@RequestBody ChatMessageRequest messageRequest) {
        ChatMessage message = ChatMessage.builder()
                .senderId(messageRequest.getSenderId())
                .receiverId(messageRequest.getReceiverId())
                .content(messageRequest.getContent())
                .timestamp(messageRequest.getTimestamp())
                .build();
        return chatService.saveMessage(message);
    }

    @GetMapping("/history")
    public Flux<ChatMessage> getChatHistory(
            @RequestParam String user1,
            @RequestParam String user2
    ) {
        return chatService.getChatHistory(user1, user2);
    }

    @GetMapping("/conversations/{userId}")
    public Flux<ConversationResponse> getUserConversations(@PathVariable String userId) {
        return conversationService.getUserConversations(userId);
    }

    @GetMapping("/conversation/{userId1}/{userId2}")
    public Flux<ChatMessage> getConversationHistory(
            @PathVariable String userId1,
            @PathVariable String userId2
    ) {
        return chatService.getChatHistory(userId1, userId2);
    }

    @GetMapping("/recent-messages/{userId}")
    public Flux<Map<String, Object>> getRecentMessages(@PathVariable String userId) {
        return chatService.getRecentMessages(userId);
    }

}