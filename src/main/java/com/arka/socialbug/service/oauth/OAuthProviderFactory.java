package com.arka.socialbug.service.oauth;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

import com.arka.socialbug.model.SocialPlatform;

@Component
public class OAuthProviderFactory {

    private final Map<SocialPlatform, OAuthProvider> providers = new EnumMap<>(SocialPlatform.class);

    public OAuthProviderFactory(InstagramOAuthProvider instagram,
                                LinkedInOAuthProvider linkedIn,
                                TwitterOAuthProvider twitter) {
        providers.put(instagram.platform(), instagram);
        providers.put(linkedIn.platform(), linkedIn);
        providers.put(twitter.platform(), twitter);
    }

    public OAuthProvider get(SocialPlatform platform) {
        OAuthProvider provider = providers.get(platform);
        if (provider == null) throw new IllegalArgumentException("Unsupported platform: " + platform);
        return provider;
    }
}
