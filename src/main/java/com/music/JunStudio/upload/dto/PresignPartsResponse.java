package com.music.JunStudio.upload.dto;

import java.util.List;
import java.util.UUID;

public record PresignPartsResponse(
        UUID uploadSessionId,
        List<PresignedPartUrl> parts
) {}