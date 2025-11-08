package com.arka.socialbug.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campaign_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    // Selected social connection (must belong to the campaign owner and match platform)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "connection_id")
    private SocialConnection connection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SocialPlatform platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CampaignItemType type;

    // Public URL to the image (uploaded via file client). Optional depending on type
    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @ElementCollection
    @CollectionTable(name = "campaign_item_hashtags", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "hashtag", length = 128)
    private List<String> hashtags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CampaignItemStatus status = CampaignItemStatus.PENDING;

    @Column(name = "scheduled_upload_at")
    private Instant scheduledUploadAt;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
