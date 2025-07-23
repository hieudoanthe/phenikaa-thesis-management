package com.phenikaa.thesisservice.dto.request;

import lombok.Data;

@Data
public class SuggestTopicDTO {
    private String title;               // Tên đề tài
    private String description;
    private String objectives;
    private String methodology;
    private String expectedOutcome;
    private Integer supervisorId;       // Chọn giảng viên mong muốn
    private String reason;              // Lý do đề xuất
}
