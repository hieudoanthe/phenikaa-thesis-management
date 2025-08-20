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
//@Table(name = "audit_log", schema = "HieuDT")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class AuditLog {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "log_id")
//    private Integer logId;
//
//    @Column(name = "user_id")
//    private Integer userId;
//
//    @Column(name = "table_name", length = 255)
//    private String tableName;
//
//    @Column(name = "action", length = 255)
//    private String action;
//
//    @Column(name = "old_data", columnDefinition = "TEXT")
//    private String oldData;
//
//    @Column(name = "new_data", columnDefinition = "TEXT")
//    private String newData;
//
//    @Column(name = "ip_address", length = 50)
//    private String ipAddress;
//
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @Column(name = "user_agent", length = 255)
//    private String userAgent;
//
//    @PrePersist
//    protected void onCreate() {
//        if (createdAt == null) {
//            createdAt = LocalDateTime.now();
//        }
//    }
//}
