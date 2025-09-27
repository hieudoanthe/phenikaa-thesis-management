package com.phenikaa.evalservice.dto;

import com.phenikaa.evalservice.entity.DefenseCommittee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseSessionExportDto {

    // Thông tin buổi bảo vệ
    private Integer sessionId;
    private String sessionName;
    private String universityName; // e.g. Đại học Phenikaa
    private LocalDate defenseDate;
    private LocalDateTime startTime;
    private String startTimeFormatted;
    private String location;
    private String status; // text (vi)
    private Integer maxStudents;

    // Thông tin sinh viên
    private Integer assignedCount;
    private Integer capacity; // = maxStudents

    // Hội đồng
    private List<CommitteeMember> committee;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommitteeMember {
        private Integer lecturerId;
        private DefenseCommittee.CommitteeRole role;
    }
}


