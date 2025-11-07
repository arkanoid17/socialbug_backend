package com.arka.socialbug.controller;

import com.arka.socialbug.dto.ConnectRequest;
import com.arka.socialbug.dto.SocialConnectionResponse;
import com.arka.socialbug.model.SocialPlatform;
import com.arka.socialbug.service.SocialConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class SocialConnectionController {

    private final SocialConnectionService connectionService;

    @GetMapping("/{platform}/authorize")
    public ResponseEntity<?> authorize(@PathVariable("platform") SocialPlatform platform,
                                       @RequestParam String redirectUri) {
        return ResponseEntity.ok(connectionService.getAuthorizationUrl(platform, redirectUri));
    }

    @PostMapping("/{platform}/callback")
    public ResponseEntity<List<SocialConnectionResponse>> callback(@PathVariable("platform") SocialPlatform platform,
                                                             @RequestBody ConnectRequest request) {
        return ResponseEntity.ok(connectionService.handleCallback(platform, request));
    }

    @GetMapping
    public ResponseEntity<List<SocialConnectionResponse>> list() {
        return ResponseEntity.ok(connectionService.listMine());
    }

    @GetMapping("/active")
    public ResponseEntity<org.springframework.data.domain.Page<SocialConnectionResponse>> listActive(
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(connectionService.listActive(pageable));
    }

    @PostMapping("/{id}/disconnect")
    public ResponseEntity<Void> disconnect(@PathVariable Long id) {
        connectionService.disconnect(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/refresh")
    public ResponseEntity<SocialConnectionResponse> refresh(@PathVariable Long id) {
        return ResponseEntity.ok(connectionService.refresh(id));
    }
}
