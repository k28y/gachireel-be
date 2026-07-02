package com.gachireel.api.application.auth.entity;

import com.gachireel.api.common.entity.CreatedAt;
import com.gachireel.api.application.user.entity.User;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken extends CreatedAt {

    @Id
    @Tsid
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(nullable = false)
    private java.time.LocalDateTime expiresAt;
}