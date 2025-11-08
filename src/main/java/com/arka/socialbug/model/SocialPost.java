package com.arka.socialbug.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "social_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "connection_id")
    private SocialConnection connection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_item_id")
    private CampaignItem campaignItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SocialPlatform platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CampaignItemType type; // POST or STORY

    @Column(length = 1024)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String caption;

    // IDs returned by the provider
    @Column(length = 128)
    private String providerCreationId; // media container id

    @Column(length = 128)
    private String providerPostId; // published media id

    private Instant publishedAt;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
