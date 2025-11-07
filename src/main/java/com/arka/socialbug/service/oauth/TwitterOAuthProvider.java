package com.arka.socialbug.service.oauth;

import com.arka.socialbug.dto.SocialProfile;
import com.arka.socialbug.dto.TokenResponse;
import com.arka.socialbug.model.SocialPlatform;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class TwitterOAuthProvider implements OAuthProvider {

    @Value("${twitter.client-id:}")
    private String clientId;

    @Value("${twitter.client-secret:}")
    private String clientSecret;

    private static final String AUTH_BASE = "https://twitter.com/i/oauth2/authorize";
    private static final String TOKEN_URL = "https://api.twitter.com/2/oauth2/token";

    @Override
    public SocialPlatform platform() { return SocialPlatform.TWITTER; }

    @Override
    public String buildAuthorizationUrl(String redirectUri, String state) {
        String scope = String.join(" ", requiredScopes());
        URI uri = UriComponentsBuilder.fromHttpUrl(AUTH_BASE)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scope)
                .queryParam("state", state)
                .queryParam("code_challenge", "CHANGE_ME_PKCE")
                .queryParam("code_challenge_method", "S256")
                .build(true).toUri();
        return uri.toString();
    }

    @Override
    public Set<String> requiredScopes() {
        Set<String> scopes = new LinkedHashSet<>();
        scopes.add("tweet.read");
        scopes.add("tweet.write");
        scopes.add("users.read");
        scopes.add("offline.access");
        return scopes;
    }

    @Override
    public TokenResponse exchangeCode(String code, String redirectUri) {
        throw new UnsupportedOperationException("Implement Twitter token exchange (OAuth 2.0 with PKCE)");
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        throw new UnsupportedOperationException("Implement Twitter refresh");
    }

    @Override
    public List<SocialProfile> fetchProfile(String accessToken) {
        throw new UnsupportedOperationException("Implement Twitter profile fetch");
    }
}
