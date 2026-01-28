package com.wedding.photo.controller;

import com.wedding.photo.dto.WeddingResponse;
import com.wedding.photo.entity.Wedding;
import com.wedding.photo.repository.WeddingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/wedding")
@CrossOrigin(origins = "*")
public class WeddingController {
    
    @Autowired
    private WeddingRepository weddingRepository;
    
    @GetMapping("/{weddingId}/details")
    public ResponseEntity<WeddingResponse> getWeddingDetails(@PathVariable UUID weddingId) {
        try {
            Optional<Wedding> weddingOpt = weddingRepository.findById(weddingId);
            
            if (weddingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Wedding wedding = weddingOpt.get();
            
            // Create public response (no sensitive data)
            WeddingResponse response = new WeddingResponse(
                wedding.getId(),
                wedding.getWeddingName(),
                wedding.getBrideName(),
                wedding.getGroomName(),
                wedding.getWeddingDate(),
                wedding.getWelcomeMessage(),
                wedding.getVenue(),
                wedding.getCeremonyTime(),
                wedding.getReceptionTime(),
                wedding.getSpecialMessage(),
                wedding.getCreatedAt(),
                null // No QR code data in public response
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}