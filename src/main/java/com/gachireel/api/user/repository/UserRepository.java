package com.gachireel.api.user.repository;

import com.gachireel.api.common.enumcode.UserRole;
import com.gachireel.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByRole(UserRole role);
    Optional<User> findByEmail(String email);
}
