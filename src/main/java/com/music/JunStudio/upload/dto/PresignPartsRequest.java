package com.music.JunStudio.upload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PresignPartsRequest(
        @NotEmpty List<@Min(1) Integer> partNumbers
) {}