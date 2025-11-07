package com.arka.socialbug.dto;

import com.arka.socialbug.model.SocialConnectionStatus;
import com.arka.socialbug.model.SocialPlatform;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class SocialConnectionResponse {
    private Long id;
    private SocialPlatform platform;
    private String externalUserId;
    private String username;
    private String displayName;
    private String profilePictureUrl;
    private SocialConnectionStatus status;
    private Instant expiresAt;
    private Instant lastSyncAt;
}
