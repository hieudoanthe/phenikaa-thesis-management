package com.phenikaa.userservice.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class CreateUserRequest {
    private String fullName;
    private String username;
    private String password;
    private Set<Integer> roleIds;
    private Integer periodId;
}
