package com.arka.socialbug.service.oauth;

import com.arka.socialbug.dto.SocialProfile;
import com.arka.socialbug.dto.TokenResponse;
import com.arka.socialbug.model.SocialPlatform;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class LinkedInOAuthProvider implements OAuthProvider {

    @Value("${linkedin.client-id:}")
    private String clientId;

    @Value("${linkedin.client-secret:}")
    private String clientSecret;

    private static final String AUTH_BASE = "https://www.linkedin.com/oauth/v2/authorization";
    private static final String TOKEN_URL = "https://www.linkedin.com/oauth/v2/accessToken";

    @Override
    public SocialPlatform platform() { return SocialPlatform.LINKEDIN; }

    @Override
    public String buildAuthorizationUrl(String redirectUri, String state) {
        String scope = String.join(" ", requiredScopes());
        URI uri = UriComponentsBuilder.fromHttpUrl(AUTH_BASE)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .queryParam("scope", scope)
                .build(true).toUri();
        return uri.toString();
    }

    @Override
    public Set<String> requiredScopes() {
        Set<String> scopes = new LinkedHashSet<>();
        scopes.add("r_liteprofile");
        scopes.add("r_emailaddress");
        scopes.add("w_member_social"); // post as member
        scopes.add("r_member_social"); // read content as member
        scopes.add("r_organization_admin");
        scopes.add("r_organization_social");
        scopes.add("w_organization_social");
        scopes.add("offline_access");
        return scopes;
    }

    @Override
    public TokenResponse exchangeCode(String code, String redirectUri) {
        throw new UnsupportedOperationException("Implement LinkedIn token exchange");
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        throw new UnsupportedOperationException("Implement LinkedIn refresh");
    }

    @Override
    public SocialProfile fetchProfile(String accessToken) {
        throw new UnsupportedOperationException("Implement LinkedIn profile fetch");
    }
}
