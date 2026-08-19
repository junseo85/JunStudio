package com.music.JunStudio.upload.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CompletePartsRequest(
        @NotEmpty List<CompletedPartRequest> parts
) {}