package com.arka.socialbug.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

import com.arka.socialbug.util.EncryptDecryptConverter;

@Entity
@Table(name = "social_connections", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "platform"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SocialPlatform platform;

    // Provider metadata
    private String externalUserId; // account/page/profile id on provider
    private String username;       // handle or page slug
    private String displayName;
    // profile/page name
    @Column(length = 4096)
    private String profilePictureUrl;

    // OAuth tokens (encrypted)
    @Convert(converter = EncryptDecryptConverter.class)
    @Column(length = 4096)
    private String accessToken;

    @Convert(converter = EncryptDecryptConverter.class)
    @Column(length = 4096)
    private String refreshToken;

    private String tokenType; // e.g., Bearer

    @Column(length = 1024)
    private String scopes; // space or comma separated

    private Instant expiresAt; // access token expiry

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SocialConnectionStatus status = SocialConnectionStatus.CONNECTED;

    private Instant lastSyncAt;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
