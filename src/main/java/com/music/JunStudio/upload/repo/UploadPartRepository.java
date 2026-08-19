package com.music.JunStudio.upload.repo;

import com.music.JunStudio.upload.domain.UploadPartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UploadPartRepository extends JpaRepository<UploadPartEntity, UploadPartEntity.Pk> {
    List<UploadPartEntity> findByUploadSessionIdOrderByPartNumberAsc(UUID uploadSessionId);
}