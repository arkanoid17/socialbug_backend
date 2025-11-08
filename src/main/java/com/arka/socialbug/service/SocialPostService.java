package com.arka.socialbug.service;

import com.arka.socialbug.dto.SocialPostResponse;
import com.arka.socialbug.model.SocialPost;
import com.arka.socialbug.model.User;
import com.arka.socialbug.repository.SocialPostRepository;
import com.arka.socialbug.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialPostService {

    private final SocialPostRepository postRepo;
    private final UserRepository userRepo;

    @Transactional(readOnly = true)
    public Page<SocialPostResponse> listMine(Pageable pageable) {
        User me = getCurrentUser();
        return postRepo
                .findByConnection_User_IdOrderByCreatedAtDesc(me.getId(), pageable)
                .map(this::map);
    }

    @Transactional(readOnly = true)
    public Page<SocialPostResponse> listByCampaign(Long campaignId, Pageable pageable) {
        // Ownership implicitly enforced because posts link to connections which link to user
        return postRepo
                .findByCampaignItem_Campaign_IdOrderByCreatedAtDesc(campaignId, pageable)
                .map(this::map);
    }

    private SocialPostResponse map(SocialPost p) {
        return new SocialPostResponse(
                p.getId(),
                p.getCampaignItem().getCampaign().getId(),
                p.getCampaignItem().getId(),
                p.getConnection().getId(),
                p.getPlatform(),
                p.getType(),
                p.getImageUrl(),
                p.getCaption(),
                p.getProviderCreationId(),
                p.getProviderPostId(),
                p.getPublishedAt(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private User getCurrentUser() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepo.findByEmail(email).orElseThrow();
    }
}
