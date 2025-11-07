package com.arka.socialbug.repository;

import com.arka.socialbug.model.SocialConnection;
import com.arka.socialbug.model.SocialConnectionStatus;
import com.arka.socialbug.model.SocialPlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SocialConnectionRepository extends JpaRepository<SocialConnection, Long> {
    List<SocialConnection> findByUser_Id(Long userId);
    Optional<SocialConnection> findByUser_IdAndPlatform(Long userId, SocialPlatform platform);
    List<SocialConnection> findByPlatformAndStatus(SocialPlatform platform, SocialConnectionStatus status);
    List<SocialConnection> findByStatusAndExpiresAtBefore(SocialConnectionStatus status, Instant before);
    Page<SocialConnection> findByUser_IdAndStatus(Long userId, SocialConnectionStatus status, Pageable pageable);
}
