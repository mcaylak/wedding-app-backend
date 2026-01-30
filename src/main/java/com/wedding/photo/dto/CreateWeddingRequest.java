package com.wedding.photo.dto;

import java.time.LocalDate;

public class CreateWeddingRequest {
    private String weddingName;
    private String weddingKey;
    private String brideName;
    private String groomName;
    private LocalDate weddingDate;
    private String welcomeMessage;
    private String venue;
    private String ceremonyTime;
    private String receptionTime;
    private String specialMessage;
    private String subdomain;
    
    public CreateWeddingRequest() {}
    
    public CreateWeddingRequest(String weddingName, String weddingKey, String brideName, String groomName, 
                               LocalDate weddingDate, String welcomeMessage, String venue, 
                               String ceremonyTime, String receptionTime, String specialMessage) {
        this.weddingName = weddingName;
        this.weddingKey = weddingKey;
        this.brideName = brideName;
        this.groomName = groomName;
        this.weddingDate = weddingDate;
        this.welcomeMessage = welcomeMessage;
        this.venue = venue;
        this.ceremonyTime = ceremonyTime;
        this.receptionTime = receptionTime;
        this.specialMessage = specialMessage;
    }
    
    public String getWeddingName() { return weddingName; }
    public void setWeddingName(String weddingName) { this.weddingName = weddingName; }
    
    public String getWeddingKey() { return weddingKey; }
    public void setWeddingKey(String weddingKey) { this.weddingKey = weddingKey; }
    
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
    
    public String getSubdomain() { return subdomain; }
    public void setSubdomain(String subdomain) { this.subdomain = subdomain; }
}