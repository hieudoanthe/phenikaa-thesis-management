package com.phenikaa.userservice.service.implement;

import com.phenikaa.dto.request.RefreshTokenRequest;
import com.phenikaa.exception.TokenRefreshException;
import com.phenikaa.userservice.entity.RefreshToken;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.repository.RefreshTokenRepository;
import com.phenikaa.userservice.repository.UserRepository;
import com.phenikaa.userservice.service.interfaces.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void save(RefreshTokenRequest request) {

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser_UserId(request.getUserId());

        if (existingToken.isPresent() && existingToken.get().getExpiryDate().isAfter(Instant.now())) {
            return;
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        RefreshToken refreshToken = existingToken.orElse(new RefreshToken());

        refreshToken.setToken(request.getToken());
        refreshToken.setExpiryDate(request.getExpiryDate());
        refreshToken.setUser(user);

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token expired!");
        }
        return token;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

}

