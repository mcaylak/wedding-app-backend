package com.wedding.photo.dto;

import java.time.LocalDateTime;

public class PhotoResponse {
    private Long id;
    private String fileName;
    private String contentType;
    private Long size;
    private LocalDateTime createdAt;
    private String downloadUrl;
    
    public PhotoResponse() {}
    
    public PhotoResponse(Long id, String fileName, String contentType, Long size, LocalDateTime createdAt, String downloadUrl) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.createdAt = createdAt;
        this.downloadUrl = downloadUrl;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}