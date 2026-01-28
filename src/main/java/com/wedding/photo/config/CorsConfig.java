package com.wedding.photo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Frontend'in port'ları (local + Railway)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:3001", 
            "http://127.0.0.1:3000",
            "http://127.0.0.1:3001",
            "https://wedding-app-frontend-production.up.railway.app"
        ));
        
        // İzin verilen HTTP methodları
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // İzin verilen header'lar
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Credentials'ı destekle
        configuration.setAllowCredentials(true);
        
        // Preflight request'leri cache'le
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}