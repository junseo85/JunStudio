package com.music.JunStudio.upload.dto;

import java.util.List;
import java.util.UUID;

public record UploadSessionStatusResponse(
        UUID uploadSessionId,
        String status,
        long chunkSize,
        int totalParts,
        List<Integer> uploadedPartNumbers
) {}