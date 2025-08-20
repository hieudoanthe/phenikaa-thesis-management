//package com.phenikaa.communicationservice.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "project_history", schema = "HieuDT")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class ProjectHistory {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "history_id")
//    private Integer historyId;
//
//    @Column(name = "topic_id", nullable = false)
//    private Integer topicId;
//
//    @Column(name = "user_id", nullable = false)
//    private Integer userId;
//
//    @Column(name = "action", length = 255)
//    private String action;
//
//    @Column(name = "old_value", columnDefinition = "TEXT")
//    private String oldValue;
//
//    @Column(name = "new_value", columnDefinition = "TEXT")
//    private String newValue;
//
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @PrePersist
//    protected void onCreate() {
//        if (createdAt == null) {
//            createdAt = LocalDateTime.now();
//        }
//    }
//}
