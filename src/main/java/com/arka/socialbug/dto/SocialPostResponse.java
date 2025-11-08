package com.arka.socialbug.dto;

import com.arka.socialbug.model.CampaignItemType;
import com.arka.socialbug.model.SocialPlatform;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class SocialPostResponse {
    private Long id;
    private Long campaignId;
    private Long itemId;
    private Long connectionId;
    private SocialPlatform platform;
    private CampaignItemType type;
    private String imageUrl;
    private String caption;
    private String providerCreationId;
    private String providerPostId;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
