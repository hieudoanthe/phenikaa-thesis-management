package com.phenikaa.submissionservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "submission_version", schema = "HieuDT")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SubmissionVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id")
    private Integer versionId;
    
    @Column(name = "submission_id")
    private Integer submissionId;
    
    @Column(name = "version_number")
    private Integer versionNumber;
    
    @Column(name = "report_title")
    private String reportTitle;
    
    @Column(name = "description", columnDefinition = "nvarchar(255)")
    private String description;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "change_log", columnDefinition = "nvarchar(255)")
    private String changeLog;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private Integer createdBy;
    
    @Column(name = "is_current")
    private Boolean isCurrent = false;
}
