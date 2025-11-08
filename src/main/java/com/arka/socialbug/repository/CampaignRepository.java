package com.arka.socialbug.repository;

import com.arka.socialbug.model.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Page<Campaign> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
