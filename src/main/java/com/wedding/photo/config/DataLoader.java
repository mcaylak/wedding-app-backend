package com.wedding.photo.config;

import com.wedding.photo.entity.Wedding;
import com.wedding.photo.repository.WeddingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private WeddingRepository weddingRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Demo data loading disabled
        System.out.println("DataLoader: No demo data will be created");
    }
}