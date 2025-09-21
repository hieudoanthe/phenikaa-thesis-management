package com.phenikaa.submissionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "report_submission", schema = "HieuDT")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ReportSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Integer submissionId;

    @Column(name = "topic_id")
    private Integer topicId;

    @Column(name = "submitted_by")
    private Integer submittedBy;

    @Column(name = "assignment_id")
    private Integer assignmentId;

    @Column(name = "report_title", nullable = false)
    private String reportTitle;

    @Column(name = "description", columnDefinition = "nvarchar(255)")
    private String description;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "submission_type")
    private Integer submissionType;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "status")
    private Integer status;

    @Column(name = "is_final")
    private Boolean isFinal;

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "parent_submission_id")
    private Integer parentSubmissionId;

    @Column(name = "change_log", columnDefinition = "nvarchar(255)")
    private String changeLog;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Feedback> feedbacks;
}
