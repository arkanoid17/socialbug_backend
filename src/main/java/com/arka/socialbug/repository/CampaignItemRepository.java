package com.arka.socialbug.repository;

import com.arka.socialbug.model.CampaignItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignItemRepository extends JpaRepository<CampaignItem, Long> {
    List<CampaignItem> findByCampaign_Id(Long campaignId);
    long countByCampaign_Id(Long campaignId);
    long countByCampaign_IdAndStatusNot(Long campaignId, com.arka.socialbug.model.CampaignItemStatus status);

    Page<CampaignItem> findByCampaign_IdOrderByCreatedAtDesc(Long campaignId, Pageable pageable);
}
