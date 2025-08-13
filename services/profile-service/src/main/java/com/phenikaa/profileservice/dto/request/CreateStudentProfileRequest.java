package com.phenikaa.profileservice.dto.request;

import lombok.Data;

@Data
public class CreateStudentProfileRequest {
    private String major;
    private String className;
    private String email;
    private String phoneNumber;
    private String avt;
}
