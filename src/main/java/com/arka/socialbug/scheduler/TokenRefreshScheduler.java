package com.arka.socialbug.scheduler;

import com.arka.socialbug.model.SocialConnection;
import com.arka.socialbug.model.SocialConnectionStatus;
import com.arka.socialbug.repository.SocialConnectionRepository;
import com.arka.socialbug.service.oauth.OAuthProviderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenRefreshScheduler {

    private final SocialConnectionRepository connectionRepository;
    private final OAuthProviderFactory providerFactory;

    // Run every hour
    @Scheduled(fixedDelayString = "PT1H")
    public void refreshExpiringTokens() {
        Instant threshold = Instant.now().plus(30, ChronoUnit.MINUTES);
        List<SocialConnection> expiring = connectionRepository
                .findByStatusAndExpiresAtBefore(SocialConnectionStatus.CONNECTED, threshold);
        for (SocialConnection c : expiring) {
            try {
                var provider = providerFactory.get(c.getPlatform());
                var refreshed = provider.refresh(c.getRefreshToken());
                c.setAccessToken(refreshed.getAccessToken());
                if (refreshed.getRefreshToken() != null && !refreshed.getRefreshToken().isBlank()) {
                    c.setRefreshToken(refreshed.getRefreshToken());
                }
                if (refreshed.getExpiresIn() > 0) {
                    c.setExpiresAt(Instant.now().plusSeconds(refreshed.getExpiresIn()));
                }
                connectionRepository.save(c);
            } catch (Exception e) {
                // mark connection as disconnected on persistent failure in real impl
            }
        }
    }
}
