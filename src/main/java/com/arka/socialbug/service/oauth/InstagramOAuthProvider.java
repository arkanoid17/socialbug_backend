package com.arka.socialbug.service.oauth;

import com.arka.socialbug.dto.SocialProfile;
import com.arka.socialbug.dto.TokenResponse;
import com.arka.socialbug.model.SocialPlatform;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class InstagramOAuthProvider implements OAuthProvider {

    @Value("${instagram.client-id:}")
    private String clientId;

    @Value("${instagram.client-secret:}")
    private String clientSecret;

    private static final String AUTH_BASE = "https://www.facebook.com/v19.0/dialog/oauth";
    private static final String TOKEN_URL = "https://graph.facebook.com/v19.0/oauth/access_token";

    private final RestClient restClient = RestClient.builder()
            .messageConverters(converters -> converters.add(new MappingJackson2HttpMessageConverter()))
            .build();
    @Override
    public SocialPlatform platform() { return SocialPlatform.INSTAGRAM; }

    @Override
    public String buildAuthorizationUrl(String redirectUri, String state) {
        String scope = String.join(",", requiredScopes());
        URI uri = UriComponentsBuilder.fromHttpUrl(AUTH_BASE)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .queryParam("scope", scope)
                .queryParam("response_type", "code")
                .build(true).toUri();
        return uri.toString();
    }

    @Override
    public Set<String> requiredScopes() {
        // For IG Professional via FB login
        Set<String> scopes = new LinkedHashSet<>();
        scopes.add("instagram_basic"); // profile/media read
        scopes.add("pages_show_list"); // list pages to find IG business account
        scopes.add("pages_read_engagement"); // read page/IG engagement
        scopes.add("pages_manage_metadata");
        scopes.add("instagram_manage_insights"); // insights
        scopes.add("instagram_content_publish"); // publish
        scopes.add("business_management"); // manage assets link
        scopes.add("public_profile");
        return scopes;
    }

    @Override
    public TokenResponse exchangeCode(String code, String redirectUri) {
        URI uri = UriComponentsBuilder.fromHttpUrl(TOKEN_URL)
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code", code)
                .queryParam("grant_type", "authorization_code")
                .build(true).toUri();

        GraphTokenResponse resp = restClient.get()
                .uri(uri)
                .retrieve()
                .body(GraphTokenResponse.class);

        if (resp == null || resp.access_token == null) {
            throw new IllegalStateException("Instagram token exchange failed: empty response");
        }
        long expiresIn = resp.expires_in != null ? resp.expires_in : 0L;
        String tokenType = resp.token_type != null ? resp.token_type : "Bearer";
        return new TokenResponse(resp.access_token, null, tokenType, expiresIn, null);
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        URI uri = UriComponentsBuilder.fromHttpUrl(TOKEN_URL)
                .queryParam("grant_type", "fb_exchange_token")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("fb_exchange_token", refreshToken)
                .build(true).toUri();

        java.util.Map resp = restClient.get()
                .uri(uri)
                .retrieve()
                .body(java.util.Map.class);

        if (resp == null || resp.get("access_token") == null) {
            throw new IllegalStateException("Instagram refresh failed: empty response");
        }
        String access = String.valueOf(resp.get("access_token"));
        Object exp = resp.get("expires_in");
        long expiresIn = exp instanceof Number ? ((Number) exp).longValue() : 0L;
        String tokenType = resp.get("token_type") != null ? String.valueOf(resp.get("token_type")) : "Bearer";
        return new TokenResponse(access, null, tokenType, expiresIn, null);
    }

//    @Override
//    public SocialProfile fetchProfile(String accessToken) {
//        String igMeEndpoint = "https://graph.instagram.com/me";
//        URI uri = UriComponentsBuilder.fromHttpUrl(igMeEndpoint)
//                .queryParam("fields", "id,username")
//                .queryParam("access_token", accessToken)
//                .build(true).toUri();
//
//        java.util.Map resp = restClient.get()
//                .uri(uri)
//                .retrieve()
//                .body(java.util.Map.class);
//
//        if (resp == null || resp.get("id") == null) {
//            throw new IllegalStateException("Instagram fetchProfile failed: empty response");
//        }
//        String id = String.valueOf(resp.get("id"));
//        String username = resp.get("username") != null ? String.valueOf(resp.get("username")) : null;
//        String name = resp.get("name") != null ? String.valueOf(resp.get("name")) : username;
//        String pictureUrl = resp.get("profile_picture_url") != null ? String.valueOf(resp.get("profile_picture_url")) : null;
//        return new SocialProfile(id, username, name, pictureUrl);
//    }

    @Override
    public SocialProfile fetchProfile(String accessToken) {
        // Step 1: Get user’s connected pages
        URI pagesUri = UriComponentsBuilder.fromHttpUrl("https://graph.facebook.com/v19.0/me/accounts")
                .queryParam("access_token", accessToken)
                .build(true)
                .toUri();

        PagesResponse pages = restClient.get()
                .uri(pagesUri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(PagesResponse.class);

        if (pages == null || pages.data == null || pages.data.isEmpty()) {
            throw new IllegalStateException("No connected Facebook pages found for user");
        }

        // Step 2: Get the Instagram business account linked to the first page
        String pageId = pages.data.get(0).id;
        URI igUri = UriComponentsBuilder.fromHttpUrl("https://graph.facebook.com/v19.0/" + pageId)
                .queryParam("fields", "instagram_business_account")
                .queryParam("access_token", accessToken)
                .build(true)
                .toUri();

        IgAccountResponse igResp = restClient.get()
                .uri(igUri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(IgAccountResponse.class);

        if (igResp == null || igResp.instagram_business_account == null) {
            throw new IllegalStateException("No Instagram business account linked to page");
        }

        String igId = igResp.instagram_business_account.id;

        // Step 3: Fetch profile details using that IG business ID
        URI profileUri = UriComponentsBuilder.fromHttpUrl("https://graph.facebook.com/v19.0/" + igId)
                .queryParam("fields", "id,username,name,profile_picture_url")
                .queryParam("access_token", accessToken)
                .build(true)
                .toUri();

        GraphProfileResponse resp = restClient.get()
                .uri(profileUri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(GraphProfileResponse.class);

        if (resp == null || resp.id == null) {
            throw new IllegalStateException("Failed to fetch Instagram business profile");
        }

//        return new SocialProfile(
//                resp.id,
//                resp.username,
//                resp.name,
//                resp.profile_picture_url,
//                "INSTAGRAM"
//        );
        return new SocialProfile(resp.id, resp.username, resp.name, resp.profile_picture_url);
    }

    // Helper DTOs
    private static class PagesResponse {
        public java.util.List<PageData> data;
        public static class PageData {
            public String id;
            public String name;
        }
    }

    private static class IgAccountResponse {
        public IgBusiness instagram_business_account;
        public static class IgBusiness {
            public String id;
        }
    }

    private static class GraphProfileResponse {
        public String id;
        public String username;
        public String name;
        public String profile_picture_url;
    }


    // Minimal mapping of Graph API token response
    private static class GraphTokenResponse {
        public String access_token;
        public String token_type;
        public Long expires_in;
    }
}
