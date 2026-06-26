package com.gachireel.api.user.repository;

import com.gachireel.api.user.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);
    void deleteByEmail(String email);
}