package com.phenikaa.userservice.repository;

import com.phenikaa.userservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByToken(String token);
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser_UserId(Integer userId);
}
