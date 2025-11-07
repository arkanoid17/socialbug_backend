package com.arka.socialbug.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionConfig {

    private static byte[] keyBytes;

    @Value("${encryption.secret:}")
    private String secret;

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            // Fallback to jwt.secret if dedicated secret not set
            secret = System.getProperty("jwt.secret", System.getenv("JWT_SECRET"));
        }
        if (secret == null || secret.length() < 32) {
            // Pad to 32 bytes for AES-256 (dev fallback). Replace in production.
            secret = (secret == null ? "dev_default_encryption_secret_32_bytes" : secret);
            secret = String.format("%-32s", secret).replace(' ', '0');
        }
        keyBytes = secret.getBytes();
    }

    public static byte[] getKeyBytes() {
        return keyBytes;
    }
}
