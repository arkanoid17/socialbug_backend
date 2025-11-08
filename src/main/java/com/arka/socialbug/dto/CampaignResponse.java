package com.arka.socialbug.dto;

import com.arka.socialbug.model.CampaignStatus;
import com.arka.socialbug.model.SocialPlatform;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class CampaignResponse {
    private Long id;
    private String name;
    private String description;
    private CampaignStatus status;
    private List<SocialPlatform> platforms;
    private Instant createdAt;
    private Instant updatedAt;
}
