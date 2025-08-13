package com.phenikaa.dto.request;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {
    private String refreshToken;
}
