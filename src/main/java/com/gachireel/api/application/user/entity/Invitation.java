package com.gachireel.api.application.user.entity;

import com.gachireel.api.common.entity.CreatedAt;
import com.gachireel.api.common.enumcode.InvitationStatus;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invitations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Invitation extends CreatedAt {

    @Id
    @Tsid
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(nullable = false)
    private java.time.LocalDateTime expiresAt;

    public void markAsUsed() {
        this.status = InvitationStatus.USED;
    }
}