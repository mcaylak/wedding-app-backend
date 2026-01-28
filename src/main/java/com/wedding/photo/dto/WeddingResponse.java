package com.wedding.photo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class WeddingResponse {
    private UUID id;
    private String weddingName;
    private String brideName;
    private String groomName;
    private LocalDate weddingDate;
    private String welcomeMessage;
    private String venue;
    private String ceremonyTime;
    private String receptionTime;
    private String specialMessage;
    private LocalDateTime createdAt;
    private String qrCodeData;
    
    public WeddingResponse() {}
    
    public WeddingResponse(UUID id, String weddingName, String brideName, String groomName, 
                          LocalDate weddingDate, String welcomeMessage, String venue,
                          String ceremonyTime, String receptionTime, String specialMessage,
                          LocalDateTime createdAt, String qrCodeData) {
        this.id = id;
        this.weddingName = weddingName;
        this.brideName = brideName;
        this.groomName = groomName;
        this.weddingDate = weddingDate;
        this.welcomeMessage = welcomeMessage;
        this.venue = venue;
        this.ceremonyTime = ceremonyTime;
        this.receptionTime = receptionTime;
        this.specialMessage = specialMessage;
        this.createdAt = createdAt;
        this.qrCodeData = qrCodeData;
    }
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getWeddingName() { return weddingName; }
    public void setWeddingName(String weddingName) { this.weddingName = weddingName; }
    
    public String getBrideName() { return brideName; }
    public void setBrideName(String brideName) { this.brideName = brideName; }
    
    public String getGroomName() { return groomName; }
    public void setGroomName(String groomName) { this.groomName = groomName; }
    
    public LocalDate getWeddingDate() { return weddingDate; }
    public void setWeddingDate(LocalDate weddingDate) { this.weddingDate = weddingDate; }
    
    public String getWelcomeMessage() { return welcomeMessage; }
    public void setWelcomeMessage(String welcomeMessage) { this.welcomeMessage = welcomeMessage; }
    
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    
    public String getCeremonyTime() { return ceremonyTime; }
    public void setCeremonyTime(String ceremonyTime) { this.ceremonyTime = ceremonyTime; }
    
    public String getReceptionTime() { return receptionTime; }
    public void setReceptionTime(String receptionTime) { this.receptionTime = receptionTime; }
    
    public String getSpecialMessage() { return specialMessage; }
    public void setSpecialMessage(String specialMessage) { this.specialMessage = specialMessage; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
}