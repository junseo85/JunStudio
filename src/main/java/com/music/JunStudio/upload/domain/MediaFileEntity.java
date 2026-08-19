package com.music.JunStudio.upload.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "media_file")
@Getter @Setter
public class MediaFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private String userId;

    @Column(name="object_key", nullable = false, unique = true)
    private String objectKey;

    @Column(name="file_name", nullable = false)
    private String fileName;

    @Column(name="content_type", nullable = false)
    private String contentType;

    @Column(name="size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name="upload_session_id", nullable = false, unique = true)
    private java.util.UUID uploadSessionId;

    @Column(name="processing_status", nullable = false)
    private String processingStatus; // PENDING, PROCESSING, DONE, FAILED

    @Column(name="created_at", nullable = false)
    private Instant createdAt;

    @Column(name="updated_at", nullable = false)
    private Instant updatedAt;
}