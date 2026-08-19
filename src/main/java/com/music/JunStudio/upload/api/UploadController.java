package com.music.JunStudio.upload.api;

import com.music.JunStudio.upload.service.UploadService;
import com.music.JunStudio.upload.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/uploads/sessions")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping
    public CreateUploadSessionResponse create(@Valid @RequestBody CreateUploadSessionRequest req) {
        return uploadService.createSession(req);
    }

    @GetMapping("/{sessionId}")
    public UploadSessionStatusResponse get(@PathVariable UUID sessionId) {
        return uploadService.getSessionStatus(sessionId);
    }

    @PostMapping("/{sessionId}/parts/presign")
    public PresignPartsResponse presign(
            @PathVariable UUID sessionId,
            @Valid @RequestBody PresignPartsRequest req
    ) {
        return uploadService.presignParts(sessionId, req.partNumbers());
    }

    @PostMapping("/{sessionId}/parts/complete")
    public void completeParts(
            @PathVariable UUID sessionId,
            @Valid @RequestBody CompletePartsRequest req
    ) {
        uploadService.completeParts(sessionId, req.parts());
    }

    @PostMapping("/{sessionId}/finalize")
    public FinalizeUploadResponse finalizeUpload(@PathVariable UUID sessionId) {
        return uploadService.finalizeUpload(sessionId);
    }

    @DeleteMapping("/{sessionId}")
    public void abort(@PathVariable UUID sessionId) {
        uploadService.abortUpload(sessionId);
    }
}