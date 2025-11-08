package com.arka.socialbug.controller;

import com.arka.socialbug.dto.SocialPostResponse;
import com.arka.socialbug.service.SocialPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class SocialPostController {

    private final SocialPostService service;

    @GetMapping
    public ResponseEntity<Page<SocialPostResponse>> listMine(Pageable pageable,
                                                             @RequestParam(value = "campaignId", required = false) Long campaignId) {
        if (campaignId != null) {
            return ResponseEntity.ok(service.listByCampaign(campaignId, pageable));
        }
        return ResponseEntity.ok(service.listMine(pageable));
    }
}
