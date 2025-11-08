package com.arka.socialbug.controller;

import com.arka.socialbug.dto.PublishResponse;
import com.arka.socialbug.service.InstagramPublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns/{campaignId}/items/{itemId}/instagram")
@RequiredArgsConstructor
public class InstagramPublishController {

    private final InstagramPublishService instagramPublishService;

    @PostMapping("/upload")
    public ResponseEntity<PublishResponse> upload(
            @PathVariable Long campaignId,
            @PathVariable Long itemId
    ) {
        return ResponseEntity.ok(instagramPublishService.publish(campaignId, itemId));
    }
}
