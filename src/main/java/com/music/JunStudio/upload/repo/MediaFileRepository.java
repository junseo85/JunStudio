package com.music.JunStudio.upload.repo;

import com.music.JunStudio.upload.domain.MediaFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaFileRepository extends JpaRepository<MediaFileEntity, Long> {
}