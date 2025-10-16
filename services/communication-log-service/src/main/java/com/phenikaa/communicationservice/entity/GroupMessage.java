package com.phenikaa.communicationservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "chat_group_messages")
public class GroupMessage {
    @Id
    private String id;
    private String groupId;
    private String senderId;
    private String content;
    private Instant timestamp;
}


