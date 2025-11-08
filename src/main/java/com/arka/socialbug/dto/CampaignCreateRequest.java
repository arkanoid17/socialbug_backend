package com.arka.socialbug.dto;

import com.arka.socialbug.model.CampaignStatus;
import com.arka.socialbug.model.SocialPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CampaignCreateRequest {
    @NotBlank
    @Size(max = 255)
    private String name;

    private String description;

    // Optional on create; defaults to ACTIVE if not provided
    private CampaignStatus status;

    @Size(min = 1, message = "At least one platform must be selected")
    private List<SocialPlatform> platforms;
}
