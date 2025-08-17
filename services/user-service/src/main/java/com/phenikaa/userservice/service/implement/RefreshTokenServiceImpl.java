package com.phenikaa.userservice.service.implement;

import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.AuthenticatedUserResponse;
import com.phenikaa.userservice.entity.RefreshToken;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.mapper.UserMapper;
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
    private final UserMapper userMapper;

    @Transactional
    public void save(SaveRefreshTokenRequest request) {

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
    public AuthenticatedUserResponse getUserByRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        return userMapper.toUserInfoResponse(user);
    }

}

