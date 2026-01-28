package com.wedding.photo.controller;

import com.wedding.photo.dto.PagedResponse;
import com.wedding.photo.dto.PhotoResponse;
import com.wedding.photo.service.AuthService;
import com.wedding.photo.service.PhotoService;
import com.wedding.photo.service.R2StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/photos")
@CrossOrigin(origins = "*")
public class PhotoController {
    
    @Autowired
    private PhotoService photoService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private R2StorageService r2StorageService;
    
    @GetMapping("/{weddingId}")
    public ResponseEntity<PagedResponse<PhotoResponse>> getPhotos(
            @PathVariable UUID weddingId,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size) {
        
        String token = extractToken(authHeader);
        UUID tokenWeddingId = authService.validateTokenAndGetWeddingId(token);
        
        if (!tokenWeddingId.equals(weddingId)) {
            return ResponseEntity.status(403).build();
        }
        
        PagedResponse<PhotoResponse> photos = photoService.getPhotosByWeddingIdPaginated(weddingId, page, size);
        return ResponseEntity.ok(photos);
    }
    
    @PostMapping("/upload/{weddingId}")
    public ResponseEntity<PhotoResponse> uploadPhoto(
            @PathVariable UUID weddingId,
            @RequestParam("file") MultipartFile file) {
        
        // Wedding ID is now available from Spring Security context
        // JWT filter already validated the token
        PhotoResponse photo = photoService.uploadPhoto(weddingId, file);
        return ResponseEntity.ok(photo);
    }
    
    @GetMapping("/download/{photoId}")
    public ResponseEntity<Resource> downloadPhoto(
            @PathVariable Long photoId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam) {
        
        String token = null;
        
        // Try to get token from query parameter first, then from header
        if (tokenParam != null && !tokenParam.isEmpty()) {
            token = tokenParam;
        } else if (authHeader != null) {
            token = extractToken(authHeader);
        }
        
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        
        UUID weddingId = authService.validateTokenAndGetWeddingId(token);
        
        try {
            // Get photo metadata from database
            PhotoResponse photo = photoService.getPhoto(weddingId, photoId);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Get file stream from R2
            String filePath = photoService.getPhotoFilePath(weddingId, photoId);
            InputStream fileStream = r2StorageService.getFileStream(filePath);
            Resource resource = new InputStreamResource(fileStream);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(photo.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "inline; filename=\"" + photo.getFileName() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @DeleteMapping("/{weddingId}/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable UUID weddingId,
            @PathVariable Long photoId,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = extractToken(authHeader);
        UUID tokenWeddingId = authService.validateTokenAndGetWeddingId(token);
        
        if (!tokenWeddingId.equals(weddingId)) {
            return ResponseEntity.status(403).build();
        }
        
        try {
            boolean deleted = photoService.deletePhoto(weddingId, photoId);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Invalid authorization header");
    }
}