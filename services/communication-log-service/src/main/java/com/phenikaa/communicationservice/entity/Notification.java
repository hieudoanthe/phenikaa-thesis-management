package com.phenikaa.communicationservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "notification")
public class Notification {
    @Id
    private String id;
    private Integer senderId;
    private Integer receiverId;
    private String message;
    private boolean read;
    private Instant createdAt;
}
