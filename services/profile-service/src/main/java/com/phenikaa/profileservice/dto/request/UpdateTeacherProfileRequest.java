package com.phenikaa.profileservice.dto.request;

import lombok.Data;

@Data
public class UpdateTeacherProfileRequest {
    private Integer userId;
    private String specialization;
    private String department;
    private String phoneNumber;
    private Integer maxStudents;
    private String avt;
}
