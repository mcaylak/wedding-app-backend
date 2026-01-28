package com.wedding.photo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AuthRequest {
    @NotNull(message = "Wedding ID is required")
    private UUID weddingId;
    
    @NotBlank(message = "Wedding key is required")
    private String weddingKey;
    
    public AuthRequest() {}
    
    public AuthRequest(UUID weddingId, String weddingKey) {
        this.weddingId = weddingId;
        this.weddingKey = weddingKey;
    }
    
    public UUID getWeddingId() { return weddingId; }
    public void setWeddingId(UUID weddingId) { this.weddingId = weddingId; }
    
    public String getWeddingKey() { return weddingKey; }
    public void setWeddingKey(String weddingKey) { this.weddingKey = weddingKey; }
}