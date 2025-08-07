package com.phenikaa.userservice.service.interfaces;

import com.phenikaa.dto.request.RefreshTokenRequest;
import com.phenikaa.userservice.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {
    void save(RefreshTokenRequest request);
    void deleteByToken(String token);
    RefreshToken verifyExpiration(RefreshToken token);
    Optional<RefreshToken> findByToken(String token);
}
