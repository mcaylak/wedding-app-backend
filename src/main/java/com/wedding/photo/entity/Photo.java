package com.wedding.photo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
public class Photo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wedding_id", nullable = false)
    private Wedding wedding;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(nullable = false)
    private String contentType;
    
    @Column(nullable = false)
    private Long size;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "TEXT")
    private String faceEncoding;
    
    private Boolean hasFace = false;
    
    public Photo() {}
    
    public Photo(Long id, Wedding wedding, String filePath, String contentType, Long size, LocalDateTime createdAt) {
        this.id = id;
        this.wedding = wedding;
        this.filePath = filePath;
        this.contentType = contentType;
        this.size = size;
        this.createdAt = createdAt;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Wedding getWedding() { return wedding; }
    public void setWedding(Wedding wedding) { this.wedding = wedding; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getFaceEncoding() { return faceEncoding; }
    public void setFaceEncoding(String faceEncoding) { this.faceEncoding = faceEncoding; }
    
    public Boolean getHasFace() { return hasFace; }
    public void setHasFace(Boolean hasFace) { this.hasFace = hasFace; }
}