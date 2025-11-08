package com.arka.socialbug.service;

import com.arka.socialbug.dto.BulkStatusUpdateRequest;
import com.arka.socialbug.dto.CampaignItemCreateRequest;
import com.arka.socialbug.dto.CampaignItemResponse;
import com.arka.socialbug.model.*;
import com.arka.socialbug.repository.CampaignItemRepository;
import com.arka.socialbug.repository.CampaignRepository;
import com.arka.socialbug.repository.SocialConnectionRepository;
import com.arka.socialbug.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CampaignItemService {

    private final CampaignRepository campaignRepository;
private final CampaignItemRepository campaignItemRepository;
    private final UserRepository userRepository;
    private final SocialConnectionRepository socialConnectionRepository;

    @Transactional
    public CampaignItemResponse create(Long campaignId, CampaignItemCreateRequest req) {
        Campaign campaign = requireMyCampaign(campaignId);

        validateAgainstCampaign(campaign, req);

        // Resolve and validate selected connection
        SocialConnection conn = socialConnectionRepository.findById(req.getConnectionId()).orElseThrow();
        // Ensure connection belongs to the same user and matches platform
        if (!Objects.equals(conn.getUser().getId(), campaign.getUser().getId())) {
            throw new IllegalArgumentException("Connection does not belong to campaign owner");
        }
        if (conn.getPlatform() != req.getPlatform()) {
            throw new IllegalArgumentException("Connection platform does not match item platform");
        }
        if (conn.getStatus() != SocialConnectionStatus.CONNECTED) {
            throw new IllegalArgumentException("Selected connection is not active");
        }

        CampaignItem item = new CampaignItem();
        item.setCampaign(campaign);
        item.setConnection(conn);
        item.setPlatform(req.getPlatform());
        item.setType(req.getType());
        item.setImageUrl(req.getImageUrl());
        item.setCaption(req.getCaption());
        item.setHashtags(req.getHashtags() != null ? new ArrayList<>(req.getHashtags()) : new ArrayList<>());
        item.setStatus(req.getStatus() != null ? req.getStatus() : CampaignItemStatus.PENDING);
        item.setScheduledUploadAt(req.getScheduledUploadAt());

        CampaignItem saved = campaignItemRepository.save(item);
        return map(saved);
    }

    @Transactional(readOnly = true)
    public Page<CampaignItemResponse> list(Long campaignId, Pageable pageable) {
        Campaign campaign = requireMyCampaign(campaignId);
        return campaignItemRepository
                .findByCampaign_IdOrderByCreatedAtDesc(campaign.getId(), pageable)
                .map(this::map);
    }

    @Transactional
    public CampaignItemResponse updateStatus(Long campaignId, Long itemId, CampaignItemStatus status) {
        Campaign campaign = requireMyCampaign(campaignId);
        CampaignItem item = campaignItemRepository.findById(itemId).orElseThrow();
        if (!Objects.equals(item.getCampaign().getId(), campaign.getId())) {
            throw new IllegalArgumentException("Item does not belong to the campaign");
        }
        item.setStatus(status);
        return map(campaignItemRepository.save(item));
    }

    @Transactional
    public int bulkUpdateStatus(Long campaignId, BulkStatusUpdateRequest req) {
        Campaign campaign = requireMyCampaign(campaignId);
        List<CampaignItem> items;
        if (req.getItemIds() == null || req.getItemIds().isEmpty()) {
            items = campaignItemRepository.findByCampaign_Id(campaign.getId());
        } else {
            items = campaignItemRepository.findAllById(req.getItemIds());
            // ensure all belong to the campaign
            for (CampaignItem it : items) {
                if (!Objects.equals(it.getCampaign().getId(), campaign.getId())) {
                    throw new IllegalArgumentException("One or more items do not belong to the campaign");
                }
            }
        }
        for (CampaignItem it : items) {
            it.setStatus(req.getStatus());
        }
        campaignItemRepository.saveAll(items);
        return items.size();
    }

    private void validateAgainstCampaign(Campaign campaign, CampaignItemCreateRequest req) {
        if (!campaign.getPlatforms().contains(req.getPlatform())) {
            throw new IllegalArgumentException("Platform not enabled for this campaign");
        }
        // Platform-specific allowed types
        boolean validType = switch (req.getPlatform()) {
            case INSTAGRAM -> (req.getType() == CampaignItemType.STORY || req.getType() == CampaignItemType.POST);
            case LINKEDIN -> (req.getType() == CampaignItemType.TEXT || req.getType() == CampaignItemType.POST);
            default -> false; // disallow others for now
        };
        if (!validType) {
            throw new IllegalArgumentException("Invalid type for the selected platform");
        }
        // Image required for STORY/POST
        if (req.getType() == CampaignItemType.STORY || req.getType() == CampaignItemType.POST) {
            if (req.getImageUrl() == null || req.getImageUrl().isBlank()) {
                throw new IllegalArgumentException("imageUrl is required for STORY/POST. Upload via file client first.");
            }
        } else { // TEXT
            if (req.getCaption() == null || req.getCaption().isBlank()) {
                throw new IllegalArgumentException("caption is required for TEXT type");
            }
            // image not required, but if provided it's okay to ignore/allow
        }
    }

    private CampaignItemResponse map(CampaignItem it) {
        return new CampaignItemResponse(
                it.getId(),
                it.getCampaign().getId(),
                it.getConnection() != null ? it.getConnection().getId() : null,
                it.getPlatform(),
                it.getType(),
                it.getImageUrl(),
                it.getCaption(),
                new ArrayList<>(it.getHashtags()),
                it.getStatus(),
                it.getScheduledUploadAt(),
                it.getCreatedAt(),
                it.getUpdatedAt()
        );
    }

    private Campaign requireMyCampaign(Long campaignId) {
        // User should be the owner of the campaign; reuse logic from CampaignService
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        var email = auth.getName();
        User me = userRepository.findByEmail(email).orElseThrow();
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
        if (!Objects.equals(campaign.getUser().getId(), me.getId())) {
            throw new IllegalArgumentException("Not your campaign");
        }
        return campaign;
    }
}
