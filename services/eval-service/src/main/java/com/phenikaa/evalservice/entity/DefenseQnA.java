package com.phenikaa.evalservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "defense_qna", schema = "HieuDT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefenseQnA {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_id")
    private Integer qnaId;

    @Column(name = "topic_id", nullable = false)
    private Integer topicId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "questioner_id", nullable = false)
    private Integer questionerId; // ID người hỏi (thành viên hội đồng)

    @Column(name = "secretary_id", nullable = false)
    private Integer secretaryId; // ID thư ký ghi lại

    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "question_time")
    private LocalDateTime questionTime;

    @Column(name = "answer_time")
    private LocalDateTime answerTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (questionTime == null) {
            questionTime = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
