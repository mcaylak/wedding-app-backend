package com.wedding.photo.repository;

import com.wedding.photo.entity.Wedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WeddingRepository extends JpaRepository<Wedding, UUID> {
}