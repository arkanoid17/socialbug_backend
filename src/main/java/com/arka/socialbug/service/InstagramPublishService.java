package com.arka.socialbug.service;

import com.arka.socialbug.dto.PublishResponse;
import com.arka.socialbug.model.*;
import com.arka.socialbug.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InstagramPublishService {

    private final CampaignItemRepository itemRepo;
    private final CampaignRepository campaignRepo;
    private final SocialConnectionRepository connectionRepo;
    private final SocialPostRepository postRepo;

    private static final String GRAPH_BASE = "https://graph.facebook.com/v21.0";

    @Transactional
    public PublishResponse publish(Long campaignId, Long itemId) {
        CampaignItem item = itemRepo.findById(itemId).orElseThrow();
        if (!Objects.equals(item.getCampaign().getId(), campaignId)) {
            throw new IllegalArgumentException("Item does not belong to campaign");
        }
        if (item.getPlatform() != SocialPlatform.INSTAGRAM) {
            throw new IllegalArgumentException("Only Instagram items can be published via this endpoint");
        }
        if (item.getType() != CampaignItemType.POST && item.getType() != CampaignItemType.STORY) {
            throw new IllegalArgumentException("Only POST or STORY supported for Instagram");
        }

        SocialConnection conn = item.getConnection();
        if (conn == null || conn.getStatus() != SocialConnectionStatus.CONNECTED) {
            throw new IllegalStateException("Connection not active");
        }

        String igUserId = conn.getExternalUserId();
        String accessToken = conn.getAccessToken();

        if (igUserId == null || igUserId.isBlank()) {
            throw new IllegalStateException("Missing Instagram user id in connection");
        }
        if (item.getImageUrl() == null || item.getImageUrl().isBlank()) {
            throw new IllegalStateException("Missing imageUrl on campaign item");
        }

        RestTemplate http = new RestTemplate();

        // ✅ Encode URL and caption safely
        String encodedImageUrl = URLEncoder.encode(item.getImageUrl(), StandardCharsets.UTF_8);
        String encodedCaption = item.getCaption() != null
                ? URLEncoder.encode(item.getCaption(), StandardCharsets.UTF_8)
                : null;

        // Step 1️⃣: Create media container
        UriComponentsBuilder mediaBuilder = UriComponentsBuilder
                .fromHttpUrl(GRAPH_BASE + "/" + igUserId + "/media")
                .queryParam("image_url", encodedImageUrl)
                .queryParam("access_token", accessToken);

        if (item.getType() == CampaignItemType.STORY) {
            mediaBuilder.queryParam("is_story", true);
        } else if (encodedCaption != null && !encodedCaption.isBlank()) {
            mediaBuilder.queryParam("caption", encodedCaption);
        }

        URI mediaUri = mediaBuilder.build(true).toUri();
        ResponseEntity<Map> mediaResp = http.postForEntity(mediaUri, null, Map.class);
        if (mediaResp.getBody() == null || !mediaResp.getBody().containsKey("id")) {
            throw new IllegalStateException("Failed to create media container");
        }

        String creationId = String.valueOf(mediaResp.getBody().get("id"));
        System.out.println("📸 Created media container: " + creationId);

        // Step 2️⃣: Poll until media is ready
        String statusUrl = GRAPH_BASE + "/" + creationId + "?fields=status_code&access_token=" + accessToken;
        String status = "IN_PROGRESS";
        int retries = 0;

        while (!"FINISHED".equals(status)) {
            try {
                Thread.sleep(3000); // Wait 3s before checking again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ResponseEntity<Map> statusResp = http.getForEntity(statusUrl, Map.class);
            if (statusResp.getBody() != null && statusResp.getBody().containsKey("status_code")) {
                status = String.valueOf(statusResp.getBody().get("status_code"));
                System.out.println("⏳ Media status: " + status);
            }

            if (++retries > 10) {
                throw new IllegalStateException("Timed out waiting for media to be ready");
            }
        }

        // Step 3️⃣: Publish media
        URI publishUri = UriComponentsBuilder
                .fromHttpUrl(GRAPH_BASE + "/" + igUserId + "/media_publish")
                .queryParam("creation_id", creationId)
                .queryParam("access_token", accessToken)
                .build(true).toUri();

        ResponseEntity<Map> publishResp = http.postForEntity(publishUri, null, Map.class);
        if (publishResp.getBody() == null || !publishResp.getBody().containsKey("id")) {
            throw new IllegalStateException("Failed to publish media");
        }

        String postId = String.valueOf(publishResp.getBody().get("id"));
        System.out.println("✅ Published media post: " + postId);

        // Step 4️⃣: Save in DB
        SocialPost post = new SocialPost();
        post.setConnection(conn);
        post.setCampaignItem(item);
        post.setPlatform(SocialPlatform.INSTAGRAM);
        post.setType(item.getType());
        post.setImageUrl(item.getImageUrl());
        post.setCaption(item.getCaption());
        post.setProviderCreationId(creationId);
        post.setProviderPostId(postId);
        post.setPublishedAt(Instant.now());
        postRepo.save(post);

        // Update campaign item status
        item.setStatus(CampaignItemStatus.UPLOADED);
        itemRepo.save(item);

        // Complete campaign if all uploaded
        long remaining = itemRepo.countByCampaign_IdAndStatusNot(campaignId, CampaignItemStatus.UPLOADED);
        if (remaining == 0) {
            Campaign campaign = item.getCampaign();
            campaign.setStatus(CampaignStatus.COMPLETED);
            campaignRepo.save(campaign);
        }

        return new PublishResponse(creationId, postId);
    }
}
