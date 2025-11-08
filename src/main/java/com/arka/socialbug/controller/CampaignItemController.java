package com.arka.socialbug.controller;

import com.arka.socialbug.dto.BulkStatusUpdateRequest;
import com.arka.socialbug.dto.CampaignItemCreateRequest;
import com.arka.socialbug.dto.CampaignItemResponse;
import com.arka.socialbug.model.CampaignItemStatus;
import com.arka.socialbug.service.CampaignItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/campaigns/{campaignId}/items")
@RequiredArgsConstructor
public class CampaignItemController {

    private final CampaignItemService service;

    @PostMapping
    public ResponseEntity<CampaignItemResponse> create(
            @PathVariable Long campaignId,
            @Valid @RequestBody CampaignItemCreateRequest request
    ) {
        CampaignItemResponse created = service.create(campaignId, request);
        return ResponseEntity.created(URI.create("/api/campaigns/" + campaignId + "/items/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<CampaignItemResponse>> list(
            @PathVariable Long campaignId,
            org.springframework.data.domain.Pageable pageable
    ) {
        return ResponseEntity.ok(service.list(campaignId, pageable));
    }

    @PatchMapping("/{itemId}/status")
    public ResponseEntity<CampaignItemResponse> updateStatus(
            @PathVariable Long campaignId,
            @PathVariable Long itemId,
            @RequestParam("status") CampaignItemStatus status
    ) {
        return ResponseEntity.ok(service.updateStatus(campaignId, itemId, status));
    }

    @PatchMapping("/status")
    public ResponseEntity<Integer> bulkUpdateStatus(
            @PathVariable Long campaignId,
            @Valid @RequestBody BulkStatusUpdateRequest request
    ) {
        int updated = service.bulkUpdateStatus(campaignId, request);
        return ResponseEntity.ok(updated);
    }
}
