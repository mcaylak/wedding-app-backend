package com.wedding.photo.service;

import com.wedding.photo.dto.PagedResponse;
import com.wedding.photo.dto.PhotoResponse;
import com.wedding.photo.entity.Photo;
import com.wedding.photo.entity.Wedding;
import com.wedding.photo.exception.InvalidFileException;
import com.wedding.photo.exception.WeddingNotFoundException;
import com.wedding.photo.repository.PhotoRepository;
import com.wedding.photo.repository.WeddingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PhotoService {
    
    private static final Logger log = LoggerFactory.getLogger(PhotoService.class);
    
    @Autowired
    private PhotoRepository photoRepository;
    
    @Autowired
    private WeddingRepository weddingRepository;
    
    @Autowired
    private R2StorageService r2StorageService;
    
    @Autowired
    private FaceRecognitionService faceRecognitionService;
    
    @Value("${app.storage.path:./uploads/weddings}")
    private String storagePath;
    
    @Value("${app.file.max-size:10485760}") // 10MB
    private long maxFileSize;
    
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png"
    );
    
    public List<PhotoResponse> getPhotosByWeddingId(UUID weddingId) {
        if (!weddingRepository.existsById(weddingId)) {
            throw new WeddingNotFoundException("Wedding not found");
        }
        
        List<Photo> photos = photoRepository.findByWeddingIdOrderByCreatedAtDesc(weddingId);
        
        return photos.stream()
                .map(photo -> new PhotoResponse(
                        photo.getId(),
                        extractFileNameFromKey(photo.getFilePath()),
                        photo.getContentType(),
                        photo.getSize(),
                        photo.getCreatedAt(),
                        "/api/photos/" + weddingId + "/download/" + photo.getId()
                ))
                .collect(Collectors.toList());
    }
    
    public PagedResponse<PhotoResponse> getPhotosByWeddingIdPaginated(UUID weddingId, int page, int size) {
        if (!weddingRepository.existsById(weddingId)) {
            throw new WeddingNotFoundException("Wedding not found");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Photo> photoPage = photoRepository.findByWeddingIdOrderByCreatedAtDesc(weddingId, pageable);
        
        List<PhotoResponse> photoResponses = photoPage.getContent().stream()
                .map(photo -> new PhotoResponse(
                        photo.getId(),
                        extractFileNameFromKey(photo.getFilePath()),
                        photo.getContentType(),
                        photo.getSize(),
                        photo.getCreatedAt(),
                        "/api/photos/" + weddingId + "/download/" + photo.getId()
                ))
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                photoResponses,
                photoPage.getNumber(),
                photoPage.getSize(),
                photoPage.getTotalElements()
        );
    }
    
    public PhotoResponse uploadPhoto(UUID weddingId, MultipartFile file) {
        validateFile(file);
        
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingNotFoundException("Wedding not found"));
        
        try {
            // Upload to R2 and get the object key
            log.info("Uploading file to R2 for wedding: {}", weddingId);
            String r2ObjectKey = r2StorageService.uploadFile(file, weddingId);
            log.info("Successfully received R2 object key: {}", r2ObjectKey);
            
            // Save photo metadata to database
            Photo photo = new Photo();
            photo.setWedding(wedding);
            photo.setFilePath(r2ObjectKey); // Store R2 object key
            photo.setContentType(file.getContentType());
            photo.setSize(file.getSize());
            
            photo = photoRepository.save(photo);
            
            // Process face recognition asynchronously
            try {
                if (faceRecognitionService.isServiceHealthy()) {
                    List<Double> faceEncoding = faceRecognitionService.extractFaceEncoding(file);
                    if (faceEncoding != null && !faceEncoding.isEmpty()) {
                        photo.setHasFace(true);
                        // Convert List<Double> to JSON string for storage
                        ObjectMapper mapper = new ObjectMapper();
                        photo.setFaceEncoding(mapper.writeValueAsString(faceEncoding));
                        photoRepository.save(photo);
                        log.info("Face encoding extracted and saved for photo: {}", photo.getId());
                    } else {
                        photo.setHasFace(false);
                        photoRepository.save(photo);
                        log.info("No face detected in photo: {}", photo.getId());
                    }
                } else {
                    log.warn("Face recognition service is not available");
                }
            } catch (Exception e) {
                log.error("Error processing face recognition for photo {}", photo.getId(), e);
                // Continue without failing the upload
            }
            
            // Extract filename from R2 object key for response
            String extractedFileName = extractFileNameFromKey(r2ObjectKey);
            
            return new PhotoResponse(
                    photo.getId(),
                    extractedFileName,
                    photo.getContentType(),
                    photo.getSize(),
                    photo.getCreatedAt(),
                    "/api/photos/download/" + photo.getId()
            );
            
        } catch (Exception e) {
            log.error("Failed to upload file for wedding {}", weddingId, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }
    
    public PhotoResponse getPhoto(UUID weddingId, Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        
        if (!photo.getWedding().getId().equals(weddingId)) {
            throw new RuntimeException("Photo does not belong to this wedding");
        }
        
        return new PhotoResponse(
                photo.getId(),
                extractFileNameFromKey(photo.getFilePath()),
                photo.getContentType(),
                photo.getSize(),
                photo.getCreatedAt(),
                "/api/photos/download/" + photo.getId()
        );
    }
    
    public String getPhotoFilePath(UUID weddingId, Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        
        if (!photo.getWedding().getId().equals(weddingId)) {
            throw new RuntimeException("Photo does not belong to this wedding");
        }
        
        return photo.getFilePath();
    }
    
    public boolean deletePhoto(UUID weddingId, Long photoId) {
        try {
            Photo photo = photoRepository.findById(photoId)
                    .orElse(null);
                    
            if (photo == null) {
                log.warn("Photo not found: {}", photoId);
                return false;
            }
            
            if (!photo.getWedding().getId().equals(weddingId)) {
                log.warn("Photo {} does not belong to wedding {}", photoId, weddingId);
                return false;
            }
            
            // Delete from R2 storage
            String filePath = photo.getFilePath();
            if (filePath != null) {
                try {
                    r2StorageService.deleteFile(filePath);
                    log.info("Successfully deleted photo file from R2: {}", filePath);
                } catch (Exception e) {
                    log.error("Failed to delete photo file from R2: {}", filePath, e);
                    // Continue with database deletion even if R2 deletion fails
                }
            }
            
            // Delete from database
            photoRepository.delete(photo);
            log.info("Successfully deleted photo from database: {}", photoId);
            
            return true;
            
        } catch (Exception e) {
            log.error("Error deleting photo {} for wedding {}", photoId, weddingId, e);
            return false;
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new InvalidFileException("File size exceeds maximum allowed size");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidFileException("Invalid file type. Only JPEG and PNG are allowed");
        }
    }
    
    private String extractFileName(String filePath) {
        return Paths.get(filePath).getFileName().toString();
    }
    
    private String extractFileNameFromKey(String r2ObjectKey) {
        if (r2ObjectKey == null) {
            return "unknown";
        }
        
        // R2 object key format: weddings/{weddingId}/photos/{fileName}
        String[] parts = r2ObjectKey.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1]; // Return the last part (filename)
        }
        
        return "unknown";
    }
}