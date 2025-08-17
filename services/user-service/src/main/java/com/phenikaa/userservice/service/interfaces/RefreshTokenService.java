package com.phenikaa.userservice.service.interfaces;

import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.AuthenticatedUserResponse;

public interface RefreshTokenService {
    void save(SaveRefreshTokenRequest request);
    void deleteByToken(String token);
    AuthenticatedUserResponse getUserByRefreshToken(String token);
}
