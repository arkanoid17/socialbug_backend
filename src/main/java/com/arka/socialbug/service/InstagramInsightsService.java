package com.arka.socialbug.service;

import com.arka.socialbug.model.CampaignItem;
import com.arka.socialbug.model.CampaignItemType;
import com.arka.socialbug.model.SocialPost;
import com.arka.socialbug.model.User;
import com.arka.socialbug.repository.SocialPostRepository;
import com.arka.socialbug.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InstagramInsightsService {

    private static final String GRAPH_BASE = "https://graph.facebook.com/v21.0";

    private final SocialPostRepository postRepo;
    private final UserRepository userRepo;

    @Transactional(readOnly = true)
    public Map<String, Object> getInsights(String providerPostId) {
        SocialPost post = postRepo.findByProviderPostId(providerPostId).orElseThrow();
        requireOwner(post);

        String accessToken = post.getConnection().getAccessToken();

        RestTemplate http = new RestTemplate();
        CampaignItemType mediaType = post.getType();
        String metrics = switch (mediaType) {
            case STORY -> "reach,replies,shares";
            case POST -> "reach,saved,likes,comments,shares,total_interactions";
            case TEXT -> "reach,saved,likes,comments,shares,total_interactions";
        };

        URI uri = UriComponentsBuilder
                .fromHttpUrl(GRAPH_BASE)
                .pathSegment(providerPostId)
                .path("/insights")
                .queryParam("metric", metrics)
                .queryParam("access_token", accessToken)
                .build(true)
                .toUri();

        ResponseEntity<Map> resp = http.getForEntity(uri, Map.class);
        return resp.getBody();

    }

    private void requireOwner(SocialPost post) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User me = userRepo.findByEmail(email).orElseThrow();
        if (!me.getId().equals(post.getConnection().getUser().getId())) {
            throw new SecurityException("Not owner of this post/connection");
        }
    }
}
