package com.phenikaa.userservice.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Integer userId;
    private String accessToken;
    private String refreshToken;
    private String role;
}