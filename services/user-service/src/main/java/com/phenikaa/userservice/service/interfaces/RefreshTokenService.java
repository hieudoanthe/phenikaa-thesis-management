package com.phenikaa.userservice.service.interfaces;

import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.UserInfoResponse;

public interface RefreshTokenService {
    void save(SaveRefreshTokenRequest request);
    void deleteByToken(String token);
    UserInfoResponse getUserByRefreshToken(String token);
}
