package com.wedding.photo.dto;

public class AuthResponse {
    private String token;
    private long expiresIn;
    private String weddingId;
    
    public AuthResponse() {}
    
    public AuthResponse(String token, long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }
    
    public AuthResponse(String token, long expiresIn, String weddingId) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.weddingId = weddingId;
    }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    
    public String getWeddingId() { return weddingId; }
    public void setWeddingId(String weddingId) { this.weddingId = weddingId; }
}