package com.arka.socialbug.service.oauth;

import com.arka.socialbug.dto.SocialProfile;
import com.arka.socialbug.dto.TokenResponse;
import com.arka.socialbug.model.SocialPlatform;

import java.util.List;
import java.util.Set;

public interface OAuthProvider {
    SocialPlatform platform();

    String buildAuthorizationUrl(String redirectUri, String state);

    Set<String> requiredScopes();

    TokenResponse exchangeCode(String code, String redirectUri);

    TokenResponse refresh(String refreshToken);

    List<SocialProfile> fetchProfile(String accessToken);
}
