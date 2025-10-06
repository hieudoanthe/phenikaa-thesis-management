package com.phenikaa.profileservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetStudentProfileResponse {
    private Integer userId;
    private String username;
    private String fullName;
    private String major;
    private String className;
    private String phoneNumber;
    private String avt;
}
