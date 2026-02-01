package com.wedding.photo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ThumbnailService {
    
    private static final int THUMBNAIL_SIZE = 400;
    private static final String THUMBNAIL_FORMAT = "jpeg";
    private static final float THUMBNAIL_QUALITY = 0.85f;
    
    @Autowired
    private R2StorageService r2StorageService;
    
    public InputStream generateThumbnail(String originalFilePath) throws IOException {
        // Get original image from R2
        InputStream originalStream = r2StorageService.getFileStream(originalFilePath);
        BufferedImage originalImage = ImageIO.read(originalStream);
        originalStream.close();
        
        if (originalImage == null) {
            throw new IOException("Could not read image from: " + originalFilePath);
        }
        
        // Calculate new dimensions maintaining aspect ratio
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        int newWidth, newHeight;
        if (originalWidth > originalHeight) {
            newWidth = THUMBNAIL_SIZE;
            newHeight = (int) ((double) originalHeight / originalWidth * THUMBNAIL_SIZE);
        } else {
            newHeight = THUMBNAIL_SIZE;
            newWidth = (int) ((double) originalWidth / originalHeight * THUMBNAIL_SIZE);
        }
        
        // Create thumbnail with high quality scaling
        BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        
        // Enable high quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw scaled image
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        // Convert to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, THUMBNAIL_FORMAT, outputStream);
        
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
    
    public String getThumbnailContentType() {
        return "image/jpeg";
    }
    
    public boolean isThumbnailCached(String filePath, Long photoId) {
        String thumbnailPath = getThumbnailPath(filePath, photoId);
        try {
            r2StorageService.getFileStream(thumbnailPath);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void cacheThumbnail(String originalFilePath, Long photoId, InputStream thumbnailStream) throws IOException {
        String thumbnailPath = getThumbnailPath(originalFilePath, photoId);
        r2StorageService.uploadFile(thumbnailPath, thumbnailStream, getThumbnailContentType());
    }
    
    public InputStream getCachedThumbnail(String originalFilePath, Long photoId) throws IOException {
        String thumbnailPath = getThumbnailPath(originalFilePath, photoId);
        return r2StorageService.getFileStream(thumbnailPath);
    }
    
    private String getThumbnailPath(String originalFilePath, Long photoId) {
        // Convert: "wedding-123/photo-456.jpg" -> "wedding-123/thumbnails/photo-[ID]-456.jpg" 
        // This ensures unique thumbnail paths even for same filenames
        String[] parts = originalFilePath.split("/");
        if (parts.length >= 2) {
            String fileName = parts[1];
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = fileName.substring(dotIndex);
                fileName = fileName.substring(0, dotIndex);
            }
            return parts[0] + "/thumbnails/photo-" + photoId + extension;
        }
        return "thumbnails/photo-" + photoId + ".jpg";
    }
}