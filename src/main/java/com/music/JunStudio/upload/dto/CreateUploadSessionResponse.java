package com.music.JunStudio.upload.dto;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateUploadSessionResponse(
        UUID uploadSessionId,
        String objectKey,
        String multipartUploadId,
        long chunkSize,
        int totalParts,
        Instant expiresAt
) {}