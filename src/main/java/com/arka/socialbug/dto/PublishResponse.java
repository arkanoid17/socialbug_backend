package com.arka.socialbug.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PublishResponse {
    private String creationId; // container id
    private String postId;     // published id
}
