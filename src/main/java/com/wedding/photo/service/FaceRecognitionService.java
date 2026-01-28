package com.wedding.photo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class FaceRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(FaceRecognitionService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.face-recognition.url:http://localhost:8082}")
    private String faceRecognitionServiceUrl;

    public FaceRecognitionService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<Double> extractFaceEncoding(MultipartFile imageFile) {
        try {
            String url = faceRecognitionServiceUrl + "/encode-face";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };
            body.add("file", resource);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                requestEntity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode encodingNode = jsonNode.get("face_encoding");
                
                List<Double> encoding = new ArrayList<>();
                for (JsonNode value : encodingNode) {
                    encoding.add(value.asDouble());
                }
                
                log.info("Successfully extracted face encoding with {} dimensions", encoding.size());
                return encoding;
            } else {
                log.error("Face recognition service returned error: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error extracting face encoding", e);
            return null;
        }
    }

    public List<Map<String, Object>> compareFaces(List<Double> referenceEncoding, List<List<Double>> targetEncodings) {
        try {
            String url = faceRecognitionServiceUrl + "/compare-faces";
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("reference_encoding", referenceEncoding);
            requestData.put("target_encodings", targetEncodings);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                requestEntity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode matchesNode = jsonNode.get("matches");
                
                List<Map<String, Object>> matches = new ArrayList<>();
                for (JsonNode match : matchesNode) {
                    Map<String, Object> matchData = new HashMap<>();
                    matchData.put("index", match.get("index").asInt());
                    matchData.put("distance", match.get("distance").asDouble());
                    matchData.put("similarity", match.get("similarity").asDouble());
                    matchData.put("is_match", match.get("is_match").asBoolean());
                    matches.add(matchData);
                }
                
                log.info("Face comparison completed for {} target faces", matches.size());
                return matches;
            } else {
                log.error("Face comparison service returned error: {}", response.getStatusCode());
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            log.error("Error comparing faces", e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> verifyFaces(MultipartFile referenceImage, List<Map<String, Object>> targetImages) {
        try {
            String url = faceRecognitionServiceUrl + "/verify-faces-simple";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // Add reference image
            ByteArrayResource resource = new ByteArrayResource(referenceImage.getBytes()) {
                @Override
                public String getFilename() {
                    return referenceImage.getOriginalFilename();
                }
            };
            body.add("reference_image", resource);
            
            // Convert target images to JSON string
            String targetImagesJson = objectMapper.writeValueAsString(targetImages);
            body.add("target_images", targetImagesJson);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                requestEntity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode matchesNode = jsonNode.get("matches");
                
                List<Map<String, Object>> matches = new ArrayList<>();
                for (JsonNode match : matchesNode) {
                    Map<String, Object> matchData = new HashMap<>();
                    matchData.put("photo_id", match.get("photo_id").asLong());
                    matchData.put("photo_name", match.get("photo_name").asText());
                    matchData.put("distance", match.get("distance").asDouble());
                    matchData.put("threshold", match.get("threshold").asDouble());
                    matchData.put("verified", match.get("verified").asBoolean());
                    matches.add(matchData);
                }
                
                log.info("DeepFace.verify completed: {} matches found", matches.size());
                return matches;
            } else {
                log.error("Face verification service returned error: {}", response.getStatusCode());
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            log.error("Error verifying faces", e);
            return new ArrayList<>();
        }
    }

    public boolean isServiceHealthy() {
        try {
            String url = faceRecognitionServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("Face recognition service health check failed", e);
            return false;
        }
    }
}