package com.phenikaa.communicationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {
    private boolean success;
    private String message;
    private int totalSent;
    private int totalFailed;
    private List<String> failedEmails;
    private List<String> successEmails;
}
