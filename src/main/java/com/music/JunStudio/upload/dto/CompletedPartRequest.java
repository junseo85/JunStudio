package com.music.JunStudio.upload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CompletedPartRequest(
        @Min(1) int partNumber,
        @NotBlank String etag,
        String checksum,
        @Positive long sizeBytes
) {}