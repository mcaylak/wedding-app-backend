package com.wedding.photo.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "weddings")
public class Wedding {
    
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private String weddingName;
    
    @Column(nullable = false)
    private String weddingKeyHash;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Dynamic wedding content fields
    @Column(nullable = true)
    private String brideName;
    
    @Column(nullable = true)
    private String groomName;
    
    @Column(nullable = true)
    private LocalDate weddingDate;
    
    @Column(nullable = true, length = 1000)
    private String welcomeMessage;
    
    @Column(nullable = true)
    private String venue;
    
    @Column(nullable = true)
    private String ceremonyTime;
    
    @Column(nullable = true)
    private String receptionTime;
    
    @Column(nullable = true, length = 2000)
    private String specialMessage;
    
    @OneToMany(mappedBy = "wedding", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Photo> photos;
    
    public Wedding() {}
    
    public Wedding(UUID id, String weddingName, String weddingKeyHash, LocalDateTime createdAt, List<Photo> photos) {
        this.id = id;
        this.weddingName = weddingName;
        this.weddingKeyHash = weddingKeyHash;
        this.createdAt = createdAt;
        this.photos = photos;
    }
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getWeddingName() { return weddingName; }
    public void setWeddingName(String weddingName) { this.weddingName = weddingName; }
    
    public String getWeddingKeyHash() { return weddingKeyHash; }
    public void setWeddingKeyHash(String weddingKeyHash) { this.weddingKeyHash = weddingKeyHash; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<Photo> getPhotos() { return photos; }
    public void setPhotos(List<Photo> photos) { this.photos = photos; }
    
    // Getters and setters for new fields
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
}