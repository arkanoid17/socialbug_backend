package com.arka.socialbug.service;

import com.arka.socialbug.dto.CampaignCreateRequest;
import com.arka.socialbug.dto.CampaignResponse;
import com.arka.socialbug.model.*;
import com.arka.socialbug.repository.CampaignRepository;
import com.arka.socialbug.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    @Transactional
    public CampaignResponse create(CampaignCreateRequest req) {
        User user = getCurrentUser();

        Campaign c = new Campaign();
        c.setUser(user);
        c.setName(req.getName());
        c.setDescription(req.getDescription());
        c.setStatus(req.getStatus() != null ? req.getStatus() : CampaignStatus.ACTIVE);
        if (req.getPlatforms() != null) {
            c.setPlatforms(req.getPlatforms().stream().filter(Objects::nonNull).collect(Collectors.toSet()));
        }

        Campaign saved = campaignRepository.save(c);
        return map(saved);
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponse> listMine(Pageable pageable) {
        User user = getCurrentUser();
        return campaignRepository
                .findByUser_IdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::map);
    }

    private CampaignResponse map(Campaign c) {
        return new CampaignResponse(
                c.getId(), c.getName(), c.getDescription(), c.getStatus(),
                new ArrayList<>(c.getPlatforms()), c.getCreatedAt(), c.getUpdatedAt()
        );
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email).orElseThrow();
    }
}
