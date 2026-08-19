package com.music.JunStudio.upload.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
public record CreateUploadSessionRequest(
        @NotBlank String fileName,
        @NotNull @Positive long fileSize,
        @NotBlank String contentType
) {}