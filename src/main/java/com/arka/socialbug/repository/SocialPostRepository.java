package com.arka.socialbug.repository;

import com.arka.socialbug.model.SocialPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialPostRepository extends JpaRepository<SocialPost, Long> {
    Optional<SocialPost> findByCampaignItem_Id(Long campaignItemId);
    Optional<SocialPost> findByProviderPostId(String providerPostId);

    Page<SocialPost> findByConnection_User_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<SocialPost> findByCampaignItem_Campaign_IdOrderByCreatedAtDesc(Long campaignId, Pageable pageable);
}
