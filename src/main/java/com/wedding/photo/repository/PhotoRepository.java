package com.wedding.photo.repository;

import com.wedding.photo.entity.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByWeddingIdOrderByCreatedAtDesc(UUID weddingId);
    Page<Photo> findByWeddingIdOrderByCreatedAtDesc(UUID weddingId, Pageable pageable);
    Page<Photo> findByWeddingIdOrderByIdAsc(UUID weddingId, Pageable pageable);
    List<Photo> findByWeddingIdAndHasFaceTrue(UUID weddingId);
    long countByWeddingId(UUID weddingId);
    long countByWeddingIdAndHasFaceTrue(UUID weddingId);
}