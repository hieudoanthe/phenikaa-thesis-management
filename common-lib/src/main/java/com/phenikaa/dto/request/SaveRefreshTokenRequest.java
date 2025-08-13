package com.phenikaa.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveRefreshTokenRequest {
    private String token;
    private Instant expiryDate;
    private Integer userId;
}