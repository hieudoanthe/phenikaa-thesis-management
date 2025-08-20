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
//@Table(name = "chat_message", schema = "HieuDT")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class ChatMessage {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "message_id")
//    private Integer messageId;
//
//    @Column(name = "sender_id", nullable = false)
//    private Integer senderId;
//
//    @Column(name = "receiver_id")
//    private Integer receiverId;
//
//    @Column(name = "topic_id")
//    private Integer topicId;
//
//    @Column(name = "content", columnDefinition = "TEXT")
//    private String content;
//
//    @Column(name = "message_type")
//    private Integer messageType;
//
//    @Column(name = "is_read")
//    private Boolean isRead;
//
//    @Column(name = "sent_at")
//    private LocalDateTime sentAt;
//
//    @Column(name = "read_at")
//    private LocalDateTime readAt;
//
//    @Column(name = "message_status")
//    private Integer messageStatus;
//
//    @PrePersist
//    protected void onCreate() {
//        if (sentAt == null) {
//            sentAt = LocalDateTime.now();
//        }
//        if (isRead == null) {
//            isRead = false;
//        }
//    }
//}
