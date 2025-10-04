package com.phenikaa.evalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentScheduleDto {
    private Integer scheduleId;
    private String eventType; // defense, meeting, deadline
    private String title;
    private LocalDate date;
    private LocalTime time;
    private String location;
    private String supervisor;
    private String status; // upcoming, urgent, completed
    private String description;
    private Integer sessionId; // For defense events
    private Integer topicId; // For topic-related events
}
