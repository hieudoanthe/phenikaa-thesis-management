package com.phenikaa.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {
    @NotNull(message = "User ID không được để trống")
    private Integer userId;
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;
    private Set<Integer> roleIds;
}
