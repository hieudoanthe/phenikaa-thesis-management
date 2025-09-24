package com.phenikaa.communicationservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodEmailRequest {
    private Long periodId;
    private String periodName;
    private String subject;
    private String message;
    private String targetDomain; // @st.phenikaa-uni.edu.vn
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status; 
    private String systemUrl;
    private String supportEmail;
    private String supportPhone;
}
