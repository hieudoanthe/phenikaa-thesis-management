package com.phenikaa.communicationservice.controller;

import com.phenikaa.communicationservice.entity.ChatMessage;
import com.phenikaa.communicationservice.service.interfaces.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public Mono<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        return chatService.saveMessage(message);
    }

    @GetMapping("/history")
    public Flux<ChatMessage> getChatHistory(
            @RequestParam String user1,
            @RequestParam String user2
    ) {
        return chatService.getChatHistory(user1, user2);
    }

}