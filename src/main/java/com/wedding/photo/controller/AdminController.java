package com.wedding.photo.controller;

import com.wedding.photo.dto.CreateWeddingRequest;
import com.wedding.photo.dto.WeddingResponse;
import com.wedding.photo.entity.Wedding;
import com.wedding.photo.repository.WeddingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private WeddingRepository weddingRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @PostMapping("/weddings")
    public ResponseEntity<WeddingResponse> createWedding(@RequestBody CreateWeddingRequest request) {
        try {
            // Create new wedding entity
            Wedding wedding = new Wedding();
            wedding.setWeddingName(request.getWeddingName());
            wedding.setWeddingKeyHash(passwordEncoder.encode(request.getWeddingKey()));
            wedding.setBrideName(request.getBrideName());
            wedding.setGroomName(request.getGroomName());
            wedding.setWeddingDate(request.getWeddingDate());
            wedding.setWelcomeMessage(request.getWelcomeMessage());
            wedding.setVenue(request.getVenue());
            wedding.setCeremonyTime(request.getCeremonyTime());
            wedding.setReceptionTime(request.getReceptionTime());
            wedding.setSpecialMessage(request.getSpecialMessage());
            
            // Save to database
            Wedding savedWedding = weddingRepository.save(wedding);
            
            // Generate QR code data (URL for accessing the wedding)
            String qrCodeData = "http://localhost:3001/?wedding=" + savedWedding.getId();
            
            // Create response
            WeddingResponse response = new WeddingResponse(
                savedWedding.getId(),
                savedWedding.getWeddingName(),
                savedWedding.getBrideName(),
                savedWedding.getGroomName(),
                savedWedding.getWeddingDate(),
                savedWedding.getWelcomeMessage(),
                savedWedding.getVenue(),
                savedWedding.getCeremonyTime(),
                savedWedding.getReceptionTime(),
                savedWedding.getSpecialMessage(),
                savedWedding.getCreatedAt(),
                qrCodeData
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/weddings")
    public ResponseEntity<List<WeddingResponse>> getAllWeddings() {
        try {
            List<Wedding> weddings = weddingRepository.findAll();
            
            List<WeddingResponse> responses = weddings.stream()
                .map(wedding -> {
                    String qrCodeData = "http://localhost:3001/?wedding=" + wedding.getId();
                    return new WeddingResponse(
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
                        qrCodeData
                    );
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/weddings/{weddingId}")
    public ResponseEntity<WeddingResponse> getWedding(@PathVariable UUID weddingId) {
        try {
            Optional<Wedding> weddingOpt = weddingRepository.findById(weddingId);
            
            if (weddingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Wedding wedding = weddingOpt.get();
            String qrCodeData = "http://localhost:3001/?wedding=" + wedding.getId();
            
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
                qrCodeData
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @PutMapping("/weddings/{weddingId}")
    public ResponseEntity<WeddingResponse> updateWedding(
            @PathVariable UUID weddingId, 
            @RequestBody CreateWeddingRequest request) {
        try {
            Optional<Wedding> weddingOpt = weddingRepository.findById(weddingId);
            
            if (weddingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Wedding wedding = weddingOpt.get();
            
            // Update fields
            if (request.getWeddingName() != null) {
                wedding.setWeddingName(request.getWeddingName());
            }
            if (request.getWeddingKey() != null) {
                wedding.setWeddingKeyHash(passwordEncoder.encode(request.getWeddingKey()));
            }
            if (request.getBrideName() != null) {
                wedding.setBrideName(request.getBrideName());
            }
            if (request.getGroomName() != null) {
                wedding.setGroomName(request.getGroomName());
            }
            if (request.getWeddingDate() != null) {
                wedding.setWeddingDate(request.getWeddingDate());
            }
            if (request.getWelcomeMessage() != null) {
                wedding.setWelcomeMessage(request.getWelcomeMessage());
            }
            if (request.getVenue() != null) {
                wedding.setVenue(request.getVenue());
            }
            if (request.getCeremonyTime() != null) {
                wedding.setCeremonyTime(request.getCeremonyTime());
            }
            if (request.getReceptionTime() != null) {
                wedding.setReceptionTime(request.getReceptionTime());
            }
            if (request.getSpecialMessage() != null) {
                wedding.setSpecialMessage(request.getSpecialMessage());
            }
            
            Wedding savedWedding = weddingRepository.save(wedding);
            String qrCodeData = "http://localhost:3001/?wedding=" + savedWedding.getId();
            
            WeddingResponse response = new WeddingResponse(
                savedWedding.getId(),
                savedWedding.getWeddingName(),
                savedWedding.getBrideName(),
                savedWedding.getGroomName(),
                savedWedding.getWeddingDate(),
                savedWedding.getWelcomeMessage(),
                savedWedding.getVenue(),
                savedWedding.getCeremonyTime(),
                savedWedding.getReceptionTime(),
                savedWedding.getSpecialMessage(),
                savedWedding.getCreatedAt(),
                qrCodeData
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @DeleteMapping("/weddings/{weddingId}")
    public ResponseEntity<Void> deleteWedding(@PathVariable UUID weddingId) {
        try {
            Optional<Wedding> weddingOpt = weddingRepository.findById(weddingId);
            
            if (weddingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            weddingRepository.deleteById(weddingId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}