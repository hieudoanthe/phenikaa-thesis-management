package com.phenikaa.communicationservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "chat_groups")
public class Group {
    @Id
    private String id;
    private String name;
    private String ownerId;
    private List<String> memberIds; 
    private Instant createdAt;
}


