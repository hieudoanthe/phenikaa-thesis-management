package com.phenikaa.communicationservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "notification")
public class Notification {
    @Id
    private String id;
    private Integer studentId;
    private Integer teacherId;
    private String message;
    private boolean read;
    private long createdAt;
}
