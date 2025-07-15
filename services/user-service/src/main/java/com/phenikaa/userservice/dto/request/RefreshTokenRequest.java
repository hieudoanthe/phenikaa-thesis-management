package com.phenikaa.userservice.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

@Getter
@Setter
public class RefreshTokenRequest {
    @NotBlank
    private String refreshToken;
}