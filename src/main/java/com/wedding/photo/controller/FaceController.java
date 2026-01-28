package com.wedding.photo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedding.photo.dto.PagedResponse;
import com.wedding.photo.dto.PhotoResponse;
import com.wedding.photo.entity.Photo;
import com.wedding.photo.repository.PhotoRepository;
import com.wedding.photo.service.AuthService;
import com.wedding.photo.service.FaceRecognitionService;
import com.wedding.photo.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/face")
@CrossOrigin(origins = "*")
public class FaceController {
    
    @Autowired
    private PhotoService photoService;
    
    @Autowired
    private FaceRecognitionService faceRecognitionService;
    
    @Autowired
    private PhotoRepository photoRepository;
    
    @Autowired
    private AuthService authService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostMapping("/search/{weddingId}")
    public ResponseEntity<PagedResponse<PhotoResponse>> searchPhotosByFace(
            @PathVariable UUID weddingId,
            @RequestParam("reference_image") MultipartFile referenceImage,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "threshold", defaultValue = "0.55") double threshold) {
        
        try {
            // Validate authorization
            String token = extractToken(authHeader);
            UUID tokenWeddingId = authService.validateTokenAndGetWeddingId(token);
            
            if (!tokenWeddingId.equals(weddingId)) {
                return ResponseEntity.status(403).build();
            }
            
            // Check if face recognition service is available
            if (!faceRecognitionService.isServiceHealthy()) {
                return ResponseEntity.status(503).build();
            }
            
            // Get all photos for this wedding
            List<Photo> allPhotos = photoRepository.findByWeddingIdOrderByCreatedAtDesc(weddingId);
            
            if (allPhotos.isEmpty()) {
                PagedResponse<PhotoResponse> emptyResponse = new PagedResponse<>(
                    Collections.emptyList(), page, size, 0L
                );
                return ResponseEntity.ok(emptyResponse);
            }
            
            // Prepare target images list with photo paths for DeepFace.verify
            List<Map<String, Object>> targetImages = new ArrayList<>();
            Map<Long, Photo> idToPhotoMap = new HashMap<>();
            
            for (Photo photo : allPhotos) {
                // Create photo URL for face recognition service to download
                String photoUrl = "http://localhost:8080/api/photos/download/" + photo.getId() + 
                    "?token=" + generatePhotoAccessToken(weddingId);
                
                Map<String, Object> imageData = new HashMap<>();
                imageData.put("id", photo.getId());
                imageData.put("name", extractFileNameFromKey(photo.getFilePath()));
                imageData.put("url", photoUrl); // Use URL instead of path
                
                targetImages.add(imageData);
                idToPhotoMap.put(photo.getId(), photo);
            }
            
            // Use DeepFace.verify method
            List<Map<String, Object>> matches = faceRecognitionService.verifyFaces(referenceImage, targetImages);
            
            // Convert matches to Photo objects
            List<Photo> matchingPhotos = matches.stream()
                .map(match -> {
                    Long photoId = (Long) match.get("photo_id");
                    return idToPhotoMap.get(photoId);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, matchingPhotos.size());
            List<Photo> paginatedPhotos = matchingPhotos.subList(start, end);
            
            // Convert to response objects
            List<PhotoResponse> photoResponses = paginatedPhotos.stream()
                .map(photo -> new PhotoResponse(
                    photo.getId(),
                    extractFileNameFromKey(photo.getFilePath()),
                    photo.getContentType(),
                    photo.getSize(),
                    photo.getCreatedAt(),
                    "/api/photos/" + weddingId + "/download/" + photo.getId()
                ))
                .collect(Collectors.toList());
            
            PagedResponse<PhotoResponse> response = new PagedResponse<>(
                photoResponses, page, size, (long) matchingPhotos.size()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/search-old/{weddingId}")
    public ResponseEntity<PagedResponse<PhotoResponse>> searchPhotosByFaceOld(
            @PathVariable UUID weddingId,
            @RequestParam("reference_image") MultipartFile referenceImage,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "threshold", defaultValue = "0.55") double threshold) {
        
        try {
            // Validate authorization
            String token = extractToken(authHeader);
            UUID tokenWeddingId = authService.validateTokenAndGetWeddingId(token);
            
            if (!tokenWeddingId.equals(weddingId)) {
                return ResponseEntity.status(403).build();
            }
            
            // Check if face recognition service is available
            if (!faceRecognitionService.isServiceHealthy()) {
                return ResponseEntity.status(503).build();
            }
            
            // Extract face encoding from reference image
            List<Double> referenceEncoding = faceRecognitionService.extractFaceEncoding(referenceImage);
            if (referenceEncoding == null || referenceEncoding.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Get all photos with face encodings for this wedding
            List<Photo> photosWithFaces = photoRepository.findByWeddingIdAndHasFaceTrue(weddingId);
            
            if (photosWithFaces.isEmpty()) {
                PagedResponse<PhotoResponse> emptyResponse = new PagedResponse<>(
                    Collections.emptyList(), page, size, 0L
                );
                return ResponseEntity.ok(emptyResponse);
            }
            
            // Prepare face encodings for comparison
            List<List<Double>> targetEncodings = new ArrayList<>();
            Map<Integer, Photo> indexToPhotoMap = new HashMap<>();
            
            int index = 0;
            for (Photo photo : photosWithFaces) {
                if (photo.getFaceEncoding() != null) {
                    try {
                        List<Double> encoding = objectMapper.readValue(
                            photo.getFaceEncoding(), 
                            new TypeReference<List<Double>>() {}
                        );
                        targetEncodings.add(encoding);
                        indexToPhotoMap.put(index, photo);
                        index++;
                    } catch (Exception e) {
                        // Skip photos with invalid encodings
                        continue;
                    }
                }
            }
            
            if (targetEncodings.isEmpty()) {
                PagedResponse<PhotoResponse> emptyResponse = new PagedResponse<>(
                    Collections.emptyList(), page, size, 0L
                );
                return ResponseEntity.ok(emptyResponse);
            }
            
            // Compare faces
            List<Map<String, Object>> matches = faceRecognitionService.compareFaces(
                referenceEncoding, targetEncodings
            );
            
            // Filter matches by threshold and sort by similarity
            List<Photo> matchingPhotos = matches.stream()
                .filter(match -> {
                    Double similarity = (Double) match.get("similarity");
                    // Use similarity threshold directly instead of double-filtering with is_match
                    return similarity >= (1.0 - threshold);
                })
                .sorted((a, b) -> {
                    Double simA = (Double) a.get("similarity");
                    Double simB = (Double) b.get("similarity");
                    return Double.compare(simB, simA); // Descending order
                })
                .map(match -> {
                    Integer matchIndex = (Integer) match.get("index");
                    return indexToPhotoMap.get(matchIndex);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, matchingPhotos.size());
            List<Photo> paginatedPhotos = matchingPhotos.subList(start, end);
            
            // Convert to response objects
            List<PhotoResponse> photoResponses = paginatedPhotos.stream()
                .map(photo -> new PhotoResponse(
                    photo.getId(),
                    extractFileNameFromKey(photo.getFilePath()),
                    photo.getContentType(),
                    photo.getSize(),
                    photo.getCreatedAt(),
                    "/api/photos/" + weddingId + "/download/" + photo.getId()
                ))
                .collect(Collectors.toList());
            
            PagedResponse<PhotoResponse> response = new PagedResponse<>(
                photoResponses, page, size, (long) matchingPhotos.size()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/stats/{weddingId}")
    public ResponseEntity<Map<String, Object>> getFaceStats(
            @PathVariable UUID weddingId,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // Validate authorization
            String token = extractToken(authHeader);
            UUID tokenWeddingId = authService.validateTokenAndGetWeddingId(token);
            
            if (!tokenWeddingId.equals(weddingId)) {
                return ResponseEntity.status(403).build();
            }
            
            long totalPhotos = photoRepository.countByWeddingId(weddingId);
            long photosWithFaces = photoRepository.countByWeddingIdAndHasFaceTrue(weddingId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total_photos", totalPhotos);
            stats.put("photos_with_faces", photosWithFaces);
            stats.put("face_recognition_enabled", faceRecognitionService.isServiceHealthy());
            
            return ResponseEntity.ok(stats);
            
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
    
    private String extractFileNameFromKey(String r2ObjectKey) {
        if (r2ObjectKey == null) {
            return "unknown";
        }
        String[] parts = r2ObjectKey.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return "unknown";
    }
    
    private String generatePhotoAccessToken(UUID weddingId) {
        // Generate a JWT token for photo access
        return authService.generateToken(weddingId);
    }
}