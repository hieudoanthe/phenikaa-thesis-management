package com.phenikaa.evalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmAutoAssignResponse {
    private boolean success;
    private int totalAssigned;
    private int createdSessions;
    private String message;
}


