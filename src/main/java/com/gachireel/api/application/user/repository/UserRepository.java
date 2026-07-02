package com.gachireel.api.application.user.repository;

import com.gachireel.api.application.user.entity.User;
import com.gachireel.api.common.enumcode.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByNicknameAndIdNot(String nickname, Long id);
    List<User> findAllByOrderByCreatedAtDesc();
    List<User> findAllByStatusOrderByCreatedAtDesc(UserStatus status);
}
