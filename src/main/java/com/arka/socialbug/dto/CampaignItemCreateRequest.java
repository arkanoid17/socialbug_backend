package com.arka.socialbug.dto;

import com.arka.socialbug.model.CampaignItemStatus;
import com.arka.socialbug.model.CampaignItemType;
import com.arka.socialbug.model.SocialPlatform;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CampaignItemCreateRequest {
    @NotNull
    private SocialPlatform platform; // must be one of the parent campaign's platforms

    @NotNull
    private CampaignItemType type; // constrained by platform

    @NotNull
    private Long connectionId; // required: selected connection

    // Image is uploaded separately; provide the public URL from FileUploadController
    private String imageUrl;

    private String caption; // mandatory only when type == TEXT

    private List<String> hashtags; // optional

    private CampaignItemStatus status; // defaults to PENDING if null

    private Instant scheduledUploadAt; // optional
}
