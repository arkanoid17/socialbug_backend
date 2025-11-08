package com.arka.socialbug.controller;

import com.arka.socialbug.dto.CampaignCreateRequest;
import com.arka.socialbug.dto.CampaignResponse;
import com.arka.socialbug.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CampaignCreateRequest request) {
        CampaignResponse created = campaignService.create(request);
        return ResponseEntity.created(URI.create("/api/campaigns/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<CampaignResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(campaignService.listMine(pageable));
    }
}
