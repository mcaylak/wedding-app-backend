package com.wedding.photo.config;

import com.wedding.photo.entity.Wedding;
import com.wedding.photo.repository.WeddingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private WeddingRepository weddingRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Test wedding oluştur
        UUID testWeddingId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        if (!weddingRepository.existsById(testWeddingId)) {
            Wedding testWedding = new Wedding();
            testWedding.setId(testWeddingId);
            testWedding.setWeddingName("Test Düğünü");
            testWedding.setWeddingKeyHash(passwordEncoder.encode("test123"));
            
            weddingRepository.save(testWedding);
            System.out.println("Test wedding created with ID: " + testWeddingId);
            System.out.println("Test wedding key: test123");
        }
    }
}