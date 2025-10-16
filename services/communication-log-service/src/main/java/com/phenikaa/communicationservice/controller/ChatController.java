package com.phenikaa.communicationservice.controller;

import com.phenikaa.communicationservice.dto.request.ChatMessageRequest;
import com.phenikaa.communicationservice.broadcaster.ChatBroadcaster;
import com.phenikaa.communicationservice.dto.response.ConversationResponse;
import com.phenikaa.communicationservice.entity.ChatMessage;
import com.phenikaa.communicationservice.entity.Group;
import com.phenikaa.communicationservice.entity.GroupMessage;
import com.phenikaa.communicationservice.repository.GroupMessageRepository;
import com.phenikaa.communicationservice.repository.GroupRepository;
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
    private final GroupRepository groupRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final ChatBroadcaster chatBroadcaster;

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

    @GetMapping("/partners/{userId}")
    public Mono<java.util.List<String>> getChatPartners(@PathVariable String userId) {
        return chatService.getDistinctPartners(userId).collectList();
    }

    // ====== GROUP CHAT APIs ======
    @PostMapping("/groups")
    public Mono<Group> createGroup(@RequestBody Group input) {
        if (input.getMemberIds() == null || input.getMemberIds().isEmpty()) {
            input.setMemberIds(java.util.List.of());
        }
        if (input.getOwnerId() != null && !input.getMemberIds().contains(input.getOwnerId())) {
            input.getMemberIds().add(input.getOwnerId());
        }
        if (input.getCreatedAt() == null) {
            input.setCreatedAt(java.time.Instant.now());
        }
        return groupRepository.save(input);
    }

    @GetMapping("/groups/{userId}")
    public Flux<Group> getMyGroups(@PathVariable String userId) {
        return groupRepository.findByMemberIdsContaining(userId);
    }

    @PostMapping("/group/send")
    public Mono<GroupMessage> sendGroupMessage(@RequestBody GroupMessage message) {
        if (message.getTimestamp() == null) message.setTimestamp(java.time.Instant.now());
        return groupMessageRepository
                .save(message)
                .flatMap(saved -> groupRepository
                        .findById(saved.getGroupId())
                        .flatMap(group -> {
                            java.util.List<String> memberIds = group.getMemberIds();
                            if (memberIds == null) memberIds = java.util.List.of();
                            return chatBroadcaster
                                    .publishGroup(saved, memberIds)
                                    .thenReturn(saved);
                        })
                        .defaultIfEmpty(saved)
                );
    }

    @GetMapping("/group/history")
    public Flux<GroupMessage> getGroupHistory(@RequestParam String groupId) {
        return groupMessageRepository.findByGroupIdOrderByTimestampAsc(groupId);
    }
}