package com.phenikaa.profileservice.dto.request;

import lombok.Data;

@Data
public class UpdateStudentProfileRequest {
    private String userId;
    private String major;
    private String className;
    private String email;
    private String phoneNumber;
    private String avt;
}
