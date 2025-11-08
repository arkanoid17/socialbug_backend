package com.arka.socialbug.controller;

import com.arka.socialbug.service.InstagramInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/instagram/insights")
@RequiredArgsConstructor
public class InstagramInsightsController {

    private final InstagramInsightsService service;

    // Users provide the Instagram media id (post id) and get insights back
    @GetMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> getInsights(@PathVariable("postId") String postId) {
        return ResponseEntity.ok(service.getInsights(postId));
    }
}
