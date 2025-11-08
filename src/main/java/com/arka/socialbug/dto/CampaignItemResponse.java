package com.arka.socialbug.dto;

import com.arka.socialbug.model.CampaignItemStatus;
import com.arka.socialbug.model.CampaignItemType;
import com.arka.socialbug.model.SocialPlatform;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class CampaignItemResponse {
    private Long id;
    private Long campaignId;
    private Long connectionId;
    private SocialPlatform platform;
    private CampaignItemType type;
    private String imageUrl;
    private String caption;
    private List<String> hashtags;
    private CampaignItemStatus status;
    private Instant scheduledUploadAt;
    private Instant createdAt;
    private Instant updatedAt;
}
