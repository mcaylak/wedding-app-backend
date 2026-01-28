package com.wedding.photo.service;

import com.wedding.photo.dto.AuthRequest;
import com.wedding.photo.dto.AuthResponse;
import com.wedding.photo.entity.Wedding;
import com.wedding.photo.exception.InvalidCredentialsException;
import com.wedding.photo.repository.WeddingRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class AuthService {
    
    @Autowired
    private WeddingRepository weddingRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:86400000}") // 24 hours default
    private long jwtExpiration;
    
    public AuthResponse verifyKey(AuthRequest request) {
        Wedding wedding = weddingRepository.findById(request.getWeddingId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid wedding ID or key"));
        
        if (!passwordEncoder.matches(request.getWeddingKey(), wedding.getWeddingKeyHash())) {
            throw new InvalidCredentialsException("Invalid wedding ID or key");
        }
        
        String token = generateToken(request.getWeddingId());
        return new AuthResponse(token, jwtExpiration, request.getWeddingId().toString());
    }
    
    public String generateToken(UUID weddingId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .setSubject(weddingId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    
    public UUID validateTokenAndGetWeddingId(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            String weddingId = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            return UUID.fromString(weddingId);
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid token");
        }
    }
}