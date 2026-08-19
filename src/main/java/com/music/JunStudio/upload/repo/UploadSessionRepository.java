package com.music.JunStudio.upload.repo;

import com.music.JunStudio.upload.domain.UploadSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UploadSessionRepository extends JpaRepository<UploadSessionEntity, UUID> {
}