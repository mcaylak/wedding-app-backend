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
        // Check if demo wedding already exists
        if (weddingRepository.count() == 0) {
            // Create demo wedding
            Wedding demoWedding = new Wedding();
            demoWedding.setWeddingName("Gazi & Selcan Düğünü");
            demoWedding.setWeddingKeyHash(passwordEncoder.encode("gazi123"));
            demoWedding.setBrideName("Selcan");
            demoWedding.setGroomName("Gazi");
            demoWedding.setWeddingDate(LocalDate.of(2024, 12, 31));
            demoWedding.setWelcomeMessage("Sevgili dostlarımız, düğün günümüzden güzel anıları paylaşmak için bu özel galeriyi hazırladık.");
            demoWedding.setVenue("Beylerbeyi Sarayı");
            demoWedding.setCeremonyTime("15:00");
            demoWedding.setReceptionTime("19:00");
            demoWedding.setSpecialMessage("Bizimle bu özel günü paylaştığınız için teşekkürler!");
            
            weddingRepository.save(demoWedding);
            System.out.println("Demo wedding created: " + demoWedding.getId());
        } else {
            System.out.println("Demo wedding already exists");
        }
    }
}