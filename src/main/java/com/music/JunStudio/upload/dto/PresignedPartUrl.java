package com.music.JunStudio.upload.dto;

public record PresignedPartUrl(
        int partNumber,
        String url
) {}