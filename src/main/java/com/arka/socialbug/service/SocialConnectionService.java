package com.arka.socialbug.service;

import com.arka.socialbug.dto.*;
import com.arka.socialbug.model.*;
import com.arka.socialbug.repository.SocialConnectionRepository;
import com.arka.socialbug.repository.UserRepository;
import com.arka.socialbug.service.oauth.OAuthProvider;
import com.arka.socialbug.service.oauth.OAuthProviderFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SocialConnectionService {

    private final SocialConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final OAuthProviderFactory providerFactory;

    public record ProviderAuthUrlResponse(String url, List<String> scopes, String state) {}

    public ProviderAuthUrlResponse getAuthorizationUrl(SocialPlatform platform, String redirectUri) {
        OAuthProvider provider = providerFactory.get(platform);
        String state = UUID.randomUUID().toString();
        String url = provider.buildAuthorizationUrl(redirectUri, state);
        return new ProviderAuthUrlResponse(url, provider.requiredScopes().stream().toList(), state);
    }

    @Transactional
    public List<SocialConnectionResponse> handleCallback(SocialPlatform platform, ConnectRequest req) {
        // 1. Get the provider and exchange the code for tokens
        OAuthProvider provider = providerFactory.get(platform);
        TokenResponse token = provider.exchangeCode(req.getCode(), req.getRedirectUri());

        // 2. Fetch all profiles from the provider
        List<SocialProfile> profiles = provider.fetchProfile(token.getAccessToken());

        // 3. Get current logged-in user
        User currentUser = getCurrentUser();

        // 4. Prepare response list
        List<SocialConnectionResponse> connections = new ArrayList<>();

        for (SocialProfile profile : profiles) {
            if (profile == null || profile.getExternalId() == null) {
                continue; // skip invalid profiles
            }

            // 5. Try to find an existing connection
            Optional<SocialConnection> optionalConn = connectionRepository
                    .findByUserAndPlatformAndExternalUserId(currentUser, platform, profile.getExternalId());

            SocialConnection conn = optionalConn.orElseGet(SocialConnection::new);

            // 6. Set/Update connection details
            conn.setUser(currentUser);
            conn.setPlatform(platform);
            conn.setAccessToken(token.getAccessToken());
            conn.setRefreshToken(token.getRefreshToken());
            conn.setTokenType(token.getTokenType());
            conn.setScopes(token.getScope());
            conn.setExpiresAt(Instant.now().plusSeconds(token.getExpiresIn()));
            conn.setStatus(SocialConnectionStatus.CONNECTED);

            conn.setExternalUserId(profile.getExternalId());
            conn.setUsername(profile.getUsername());
            conn.setDisplayName(profile.getDisplayName());
            conn.setProfilePictureUrl(profile.getPictureUrl());

            // 7. Save the connection safely
            try {
                SocialConnection saved = connectionRepository.save(conn);
                connections.add(map(saved));
            } catch (Exception e) {
                // Optional: log and skip duplicates
                System.out.println("Could not save connection for externalId="
                        + profile.getExternalId() + " due to " + e.getMessage());
            }
        }

        return connections;
    }


    @Transactional(readOnly = true)
    public List<SocialConnectionResponse> listMine() {
        User currentUser = getCurrentUser();
        return connectionRepository.findByUser_Id(currentUser.getId()).stream().map(this::map).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<SocialConnectionResponse> listActive(org.springframework.data.domain.Pageable pageable) {
        User currentUser = getCurrentUser();
        return connectionRepository
                .findByUser_IdAndStatus(currentUser.getId(), SocialConnectionStatus.CONNECTED, pageable)
                .map(this::map);
    }

    @Transactional
    public void disconnect(Long connectionId) {
        SocialConnection conn = connectionRepository.findById(connectionId).orElseThrow();
        requireOwner(conn);
        conn.setStatus(SocialConnectionStatus.DISCONNECTED);
        conn.setAccessToken(null);
        conn.setRefreshToken(null);
        connectionRepository.save(conn);
    }

    @Transactional
    public SocialConnectionResponse refresh(Long connectionId) {
        SocialConnection conn = connectionRepository.findById(connectionId).orElseThrow();
        requireOwner(conn);
        OAuthProvider provider = providerFactory.get(conn.getPlatform());
        TokenResponse refreshed = provider.refresh(conn.getRefreshToken());
        conn.setAccessToken(refreshed.getAccessToken());
        if (refreshed.getRefreshToken() != null && !refreshed.getRefreshToken().isBlank()) {
            conn.setRefreshToken(refreshed.getRefreshToken());
        }
        conn.setTokenType(refreshed.getTokenType());
        conn.setScopes(refreshed.getScope());
        if (refreshed.getExpiresIn() > 0) {
            conn.setExpiresAt(Instant.now().plusSeconds(refreshed.getExpiresIn()));
        }
        SocialConnection saved = connectionRepository.save(conn);
        return map(saved);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    private void requireOwner(SocialConnection conn) {
        if (!getCurrentUser().getId().equals(conn.getUser().getId())) {
            throw new SecurityException("Not owner of this connection");
        }
    }

    private SocialConnectionResponse map(SocialConnection c) {
        return new SocialConnectionResponse(
                c.getId(), c.getPlatform(), c.getExternalUserId(), c.getUsername(), c.getDisplayName(),
                c.getProfilePictureUrl(), c.getStatus(), c.getExpiresAt(), c.getLastSyncAt()
        );
    }
}
