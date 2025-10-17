package com.phenikaa.thesisservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "suggestion_history", schema = "HieuDT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "request_text", columnDefinition = "nvarchar(1000)")
    private String requestText;

    @Column(name = "specialization", columnDefinition = "nvarchar(255)")
    private String specialization;

    @Column(name = "suggestions_json", columnDefinition = "nvarchar(max)")
    private String suggestionsJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}


