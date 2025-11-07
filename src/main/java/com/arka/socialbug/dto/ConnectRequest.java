package com.arka.socialbug.dto;

import lombok.Data;

@Data
public class ConnectRequest {
    private String code;
    private String redirectUri;
}
