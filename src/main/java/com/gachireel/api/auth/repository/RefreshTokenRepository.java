package com.gachireel.api.auth.repository;

import com.gachireel.api.auth.entity.RefreshToken;
import com.gachireel.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    void deleteByExpiresAtBefore(LocalDateTime now); // 만료 토큰 정리용
}