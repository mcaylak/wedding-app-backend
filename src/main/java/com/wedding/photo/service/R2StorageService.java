package com.wedding.photo.service;

import com.wedding.photo.config.R2Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class R2StorageService {

    private static final Logger log = LoggerFactory.getLogger(R2StorageService.class);

    @Autowired
    @Qualifier("r2Client")
    private S3Client s3Client;

    @Autowired
    private R2Config r2Config;

    public void ensureBucketExists() throws Exception {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .build();
            s3Client.headBucket(headBucketRequest);
            log.info("Bucket '{}' already exists", r2Config.getBucketName());
        } catch (NoSuchBucketException e) {
            // Bucket doesn't exist, create it
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .build();
            s3Client.createBucket(createBucketRequest);
            log.info("Created bucket: {}", r2Config.getBucketName());
        } catch (Exception e) {
            log.error("Failed to check or create bucket", e);
            throw e;
        }
    }

    public String uploadFile(MultipartFile file, UUID weddingId) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        String objectKey = String.format("weddings/%s/photos/%s", weddingId.toString(), fileName);
        
        log.info("Uploading file to R2 with key: {}", objectKey);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Successfully uploaded file to R2: {}", objectKey);
            return objectKey;
        } catch (Exception e) {
            log.error("Failed to upload file to R2: {}", objectKey, e);
            throw new IOException("Failed to upload file to R2", e);
        }
    }

    public InputStream getFileStream(String objectKey) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(objectKey)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            log.error("Failed to get file from R2: {}", objectKey, e);
            throw new IOException("Failed to get file from R2", e);
        }
    }

    public void uploadFile(String objectKey, InputStream inputStream, String contentType) throws IOException {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            // Convert InputStream to byte array to get content length
            byte[] bytes = inputStream.readAllBytes();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
            log.info("Successfully uploaded file to R2: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to upload file to R2: {}", objectKey, e);
            throw new IOException("Failed to upload file to R2", e);
        }
    }

    public void deleteFile(String objectKey) throws IOException {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file from R2: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to delete file from R2: {}", objectKey, e);
            throw new IOException("Failed to delete file from R2", e);
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}