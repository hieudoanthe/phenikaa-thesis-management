package com.phenikaa.profileservice.dto.response;

import lombok.Data;

@Data
public class GetTeacherProfileResponse {
    private Integer userId;
    private String fullName;
    private String specialization;
    private String department;
    private String phoneNumber;
    private Integer maxStudents;
    private String avt;
}
