package com.wedding.photo;

import com.wedding.photo.service.R2StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WeddingPhotoApplication {

    @Autowired
    private R2StorageService r2StorageService;

    public static void main(String[] args) {
        SpringApplication.run(WeddingPhotoApplication.class, args);
    }

    @Bean
    public ApplicationRunner initializeR2() {
        return args -> {
            try {
                r2StorageService.ensureBucketExists();
            } catch (Exception e) {
                System.err.println("Warning: Failed to initialize R2 bucket: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}