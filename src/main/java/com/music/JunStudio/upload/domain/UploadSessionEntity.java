package com.music.JunStudio.upload.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "upload_session")
@Getter @Setter
public class UploadSessionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "object_key", nullable = false, unique = true)
    private String objectKey;

    @Column(name = "provider_upload_id", nullable = false)
    private String providerUploadId;

    @Column(name = "chunk_size_bytes", nullable = false)
    private long chunkSizeBytes;

    @Column(name = "total_parts", nullable = false)
    private int totalParts;

    @Column(name = "status", nullable = false)
    private String status; // INIT, UPLOADING, COMPLETED, ABORTED, EXPIRED

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}