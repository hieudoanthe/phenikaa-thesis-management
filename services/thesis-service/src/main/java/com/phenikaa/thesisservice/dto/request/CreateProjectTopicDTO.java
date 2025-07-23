package com.phenikaa.thesisservice.dto.request;

import com.phenikaa.thesisservice.entity.ProjectTopic;
import lombok.Data;

@Data
public class CreateProjectTopicDTO {
    private String topicCode;
    private String title;             // Tiêu đề đề tài
    private String description;       // Mô tả chi tiết
    private String objectives;        // Mục tiêu
    private String methodology;       // Phương pháp
    private String expectedOutcome;   // Kết quả mong đợi
    private Integer academicYearId;   // ID năm học
    private Integer maxStudents;      // Số sinh viên tối đa
    private ProjectTopic.DifficultyLevel difficultyLevel;   // "EASY", "MEDIUM", "HARD"
}
