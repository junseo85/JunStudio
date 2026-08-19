package com.music.JunStudio.upload.dto;

public record FinalizeUploadResponse(
        Long mediaFileId,
        String processingStatus
) {}