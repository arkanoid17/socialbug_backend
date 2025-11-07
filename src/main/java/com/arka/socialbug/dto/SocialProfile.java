package com.arka.socialbug.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialProfile {
    private String externalId;
    private String username;
    private String displayName;
    private String pictureUrl;
}
