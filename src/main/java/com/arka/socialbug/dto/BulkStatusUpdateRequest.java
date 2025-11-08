package com.arka.socialbug.dto;

import com.arka.socialbug.model.CampaignItemStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkStatusUpdateRequest {
    @NotNull
    private CampaignItemStatus status;

    // Optional: if provided, update only these ids; otherwise apply to all items in the campaign
    private List<Long> itemIds;
}
