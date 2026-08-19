package com.music.JunStudio.upload.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "upload_part")
@Getter @Setter
@IdClass(UploadPartEntity.Pk.class)
public class UploadPartEntity {

    @Id
    @Column(name = "upload_session_id", nullable = false)
    private UUID uploadSessionId;

    @Id
    @Column(name = "part_number", nullable = false)
    private int partNumber;

    @Column(name = "etag", nullable = false)
    private String etag;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Getter @Setter
    public static class Pk implements Serializable {
        private UUID uploadSessionId;
        private int partNumber;
    }
}